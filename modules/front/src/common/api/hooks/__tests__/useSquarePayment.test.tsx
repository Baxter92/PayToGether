import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useSquarePayment } from "../useSquarePayment";

// Mock du window.Square
const mockSquare = {
  payments: vi.fn(),
};

// Mock du apiClient
vi.mock("@/common/api/apiClient", () => ({
  apiClient: {
    post: vi.fn(),
    get: vi.fn(),
  },
}));

describe("useSquarePayment", () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    });

    // Mock window.Square
    (window as any).Square = mockSquare;

    // Mock du script Square
    const mockScript = document.createElement("script");
    mockScript.src = "https://sandbox.web.squarecdn.com/v1/square.js";
    document.body.appendChild(mockScript);

    // Simuler le chargement du script
    setTimeout(() => {
      mockScript.onload?.(new Event("load"));
    }, 0);
  });

  afterEach(() => {
    queryClient.clear();
    delete (window as any).Square;
    vi.clearAllMocks();
  });

  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );

  it("devrait charger le SDK Square", async () => {
    const { result } = renderHook(() => useSquarePayment(), { wrapper });

    await waitFor(() => {
      expect(result.current.isSquareLoaded).toBe(true);
    });
  });

  it("devrait gérer les erreurs de chargement du SDK", async () => {
    delete (window as any).Square;

    const { result } = renderHook(() => useSquarePayment(), { wrapper });

    // Simuler une erreur de chargement
    const scripts = document.querySelectorAll('script[src*="square"]');
    scripts.forEach((script) => {
      script.dispatchEvent(new Event("error"));
    });

    await waitFor(() => {
      expect(result.current.squareError).toBeTruthy();
    });
  });

  it("devrait exposer la fonction createPayment", () => {
    const { result } = renderHook(() => useSquarePayment(), { wrapper });

    expect(result.current.createPayment).toBeDefined();
    expect(typeof result.current.createPayment).toBe("function");
  });

  it("devrait exposer la fonction checkPaymentStatus", () => {
    const { result } = renderHook(() => useSquarePayment(), { wrapper });

    expect(result.current.checkPaymentStatus).toBeDefined();
    expect(typeof result.current.checkPaymentStatus).toBe("function");
  });

  it("devrait exposer la fonction refundPayment", () => {
    const { result } = renderHook(() => useSquarePayment(), { wrapper });

    expect(result.current.refundPayment).toBeDefined();
    expect(typeof result.current.refundPayment).toBe("function");
  });

  it("devrait retourner l'état de chargement initial", () => {
    const { result } = renderHook(() => useSquarePayment(), { wrapper });

    expect(result.current.isSquareLoaded).toBeDefined();
    expect(result.current.squareError).toBeNull();
    expect(result.current.isCreatingPayment).toBe(false);
  });
});

