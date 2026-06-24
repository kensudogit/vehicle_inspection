const BACKEND_HEALTH = "http://127.0.0.1:8080/api/health";

export async function GET() {
  try {
    const res = await fetch(BACKEND_HEALTH, { cache: "no-store" });
    const body = await res.json();
    return Response.json(body, { status: res.status });
  } catch {
    return Response.json({ status: "unavailable", service: "vehicle-inspection" }, { status: 503 });
  }
}
