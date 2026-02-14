import type { ApiPlugin, RefreshTokenConfig, RequestContext } from "../types";
import { HttpError } from "../HttpError";

interface QueuedRequest {
  resolve: (value: any) => void;
  reject: (reason?: any) => void;
  ctx: RequestContext;
}

export const refreshTokenPlugin = (
  cfg: RefreshTokenConfig,
  replayRequest: (ctx: RequestContext) => Promise<any>,
): ApiPlugin => {
  if (!cfg.enabled) return {};

  let isRefreshing = false;
  let queue: QueuedRequest[] = [];

  const processQueueSuccess = async () => {
    const items = queue.slice();
    queue = [];
    for (const q of items) {
      try {
        const res = await replayRequest(q.ctx);
        q.resolve(res);
      } catch (e) {
        q.reject(e);
      }
    }
  };

  const processQueueFailure = (error: any) => {
    const items = queue.slice();
    queue = [];
    for (const q of items) q.reject(error);
  };

  return {
    onError: async (error: any, ctx: RequestContext) => {
      // We only handle HttpError 401 here
      if (!(error instanceof HttpError) || error.status !== 401)
        return undefined;

      // If the failed request was the refresh endpoint itself -> give up
      if (ctx.url.includes(cfg.refreshEndpoint)) {
        cfg.onRefreshFailure?.(error);
        return undefined;
      }

      // If already refreshing, queue the request and return a Promise that resolves when replayed
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          queue.push({ resolve, reject, ctx });
        });
      }

      // Start refresh
      isRefreshing = true;
      try {
        const refreshToken = await cfg?.getRefreshToken?.();
        if (!refreshToken) throw error;

        const r = await fetch(cfg.refreshEndpoint, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ refreshToken }),
        });

        if (!r.ok) {
          const he = await HttpError.fromResponse(r);
          throw he;
        }

        const data = await r.json();
        await cfg.saveTokens(data.accessToken, data.refreshToken);

        isRefreshing = false;

        // Replay queued requests
        await processQueueSuccess();

        // Finally replay the original failed request and return its result to caller
        return replayRequest(ctx);
      } catch (err) {
        isRefreshing = false;
        processQueueFailure(err);
        cfg.onRefreshFailure?.(err);
        throw err;
      }
    },
  };
};
