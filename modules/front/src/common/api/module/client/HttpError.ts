// Wrapper d'erreur HTTP: contient status + body (si JSON)
export class HttpError extends Error {
  public status: number;
  public body: any | null;

  constructor(status: number, body: any | null = null, message?: string) {
    super(message ?? (body?.message || `HTTP ${status}`));
    this.status = status;
    this.body = body;
    Object.setPrototypeOf(this, HttpError.prototype);
  }

  static async fromResponse(res: Response): Promise<HttpError> {
    const contentType = res.headers.get("content-type") || "";
    let body = null;
    if (contentType.includes("application/json")) {
      body = await res.json().catch(() => null);
    } else {
      body = await res.text().catch(() => null);
    }
    return new HttpError(res.status, body);
  }

  isClientError() {
    return this.status >= 400 && this.status < 500;
  }
  isServerError() {
    return this.status >= 500;
  }
}
