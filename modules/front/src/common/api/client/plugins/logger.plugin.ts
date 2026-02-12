import type { ApiPlugin } from "../types";

export const loggerPlugin = (): ApiPlugin => ({
  onRequest: (ctx) => {
    (ctx as any)._start = Date.now();
    console.debug(`[API → request] ${ctx.method} ${ctx.url}`);
  },
  onResponse: (res, ctx) => {
    const duration = Date.now() - ((ctx as any)._start ?? Date.now());
    console.debug(
      `[API ← response] ${ctx.method} ${ctx.url} ${res.status} (${duration}ms)`,
    );
    return res;
  },
  onError: (err, ctx) => {
    console.error(`[API x error] ${ctx.method} ${ctx.url}`, err);
  },
});
