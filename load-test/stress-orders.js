import http from "k6/http";
import { check } from "k6";
import { Counter, Rate, Trend } from "k6/metrics";
import { SharedArray } from "k6/data";

const BASE_URL = __ENV.BASE_URL || "https://32.199.155.252.sslip.io";
const PRODUCT_ID = __ENV.PRODUCT_ID || "018f9000-0000-7000-8000-000000000301";
const PASSWORD = __ENV.LOAD_TEST_PASSWORD || "Password123!";
const RATE = Number(__ENV.RATE || 10);
const DURATION = __ENV.DURATION || "15s";
const PRE_ALLOCATED_VUS = Number(__ENV.PRE_ALLOCATED_VUS || 50);
const MAX_VUS = Number(__ENV.MAX_VUS || 1000);
const tokenPool = __ENV.TOKENS_FILE
  ? new SharedArray("load-test-tokens", () => JSON.parse(open(__ENV.TOKENS_FILE)))
  : null;

const orderSuccess = new Rate("order_success");
const orderFailures = new Counter("order_failures");
const orderDuration = new Trend("order_creation_duration", true);

export const options = {
  scenarios: {
    order_creation: {
      executor: "constant-arrival-rate",
      rate: RATE,
      timeUnit: "1s",
      duration: DURATION,
      preAllocatedVUs: PRE_ALLOCATED_VUS,
      maxVUs: MAX_VUS,
    },
  },
  thresholds: {
    order_success: ["rate>0.95"],
    http_req_failed: ["rate<0.05"],
    order_creation_duration: ["p(95)<2000"],
  },
};

let accessToken;
let currentAddressId;
let loggedError = false;

function paddedVuId() {
  return String(__VU).padStart(4, "0");
}

function addressId() {
  return currentAddressId || `20000000-0000-7000-8000-${String(__VU).padStart(12, "0")}`;
}

function authenticate() {
  if (accessToken) {
    return true;
  }

  if (tokenPool) {
    const identity = tokenPool[(__VU - 1) % tokenPool.length];
    accessToken = identity.accessToken;
    currentAddressId = identity.deliveryAddressId;
    return true;
  }

  const response = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({
      email: `loadtest-${paddedVuId()}@example.com`,
      password: PASSWORD,
    }),
    { headers: { "Content-Type": "application/json" } },
  );

  const valid = check(response, {
    "login returns 200": (result) => result.status === 200,
  });
  if (!valid) {
    logFirstError("login", response);
    return false;
  }

  accessToken = response.json("accessToken");
  currentAddressId = addressId();
  return Boolean(accessToken);
}

function authenticatedParams() {
  return {
    headers: {
      Authorization: `Bearer ${accessToken}`,
      "Content-Type": "application/json",
    },
  };
}

function logFirstError(step, response) {
  if (!loggedError) {
    console.error(`${step} failed: ${response.status} ${response.body}`);
    loggedError = true;
  }
}

export default function () {
  if (!authenticate()) {
    orderSuccess.add(false);
    orderFailures.add(1);
    return;
  }

  const cartResponse = http.post(
    `${BASE_URL}/api/cart/items`,
    JSON.stringify({ productId: PRODUCT_ID, quantity: 1 }),
    authenticatedParams(),
  );
  if (cartResponse.status !== 201) {
    check(cartResponse, { "cart item returns 201": () => false });
    orderSuccess.add(false);
    orderFailures.add(1);
    logFirstError("cart", cartResponse);
    return;
  }

  const startedAt = Date.now();
  const orderResponse = http.post(
    `${BASE_URL}/api/orders`,
    JSON.stringify({
      deliveryAddressId: addressId(),
      tipAmount: 0,
      couponCode: null,
      notes: "Prueba de carga aislada con k6",
      useLoyaltyPoints: false,
      useDigitalWallet: false,
    }),
    authenticatedParams(),
  );
  orderDuration.add(Date.now() - startedAt);

  const created = check(orderResponse, {
    "order returns 201": (result) => result.status === 201,
  });
  orderSuccess.add(created);
  if (!created) {
    orderFailures.add(1);
    logFirstError("order", orderResponse);
  }
}
