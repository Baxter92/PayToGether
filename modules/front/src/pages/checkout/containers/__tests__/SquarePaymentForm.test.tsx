import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import SquarePaymentForm from "../SquarePaymentForm";

// Mock du useSquarePayment
vi.mock("@/common/api/hooks/useSquarePayment", () => ({
  useSquarePayment: () => ({
    isSquareLoaded: true,
    squareError: null,
    createPayment: vi.fn().mockResolvedValue({
      uuid: "test-payment-id",
      statut: "CONFIRME",
    }),
    isCreatingPayment: false,
    paymentError: null,
    checkPaymentStatus: vi.fn(),
    refundPayment: vi.fn(),
  }),
}));

describe("SquarePaymentForm", () => {
  let queryClient: QueryClient;
  const mockOnSuccess = vi.fn();
  const mockOnError = vi.fn();

  const defaultProps = {
    commandeUuid: "test-commande-uuid",
    utilisateurUuid: "test-user-uuid",
    montant: 99.99,
    onSuccess: mockOnSuccess,
    onError: mockOnError,
  };

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    });

    vi.clearAllMocks();

    // Mock window.Square
    (window as any).Square = {
      payments: vi.fn(() => ({
        card: vi.fn().mockResolvedValue({
          attach: vi.fn(),
          tokenize: vi.fn().mockResolvedValue({
            status: "OK",
            token: "test-token",
          }),
          destroy: vi.fn(),
        }),
      })),
    };
  });

  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );

  it("devrait afficher le chargement pendant l'initialisation", () => {
    const mockUseSquarePayment = vi.fn(() => ({
      isSquareLoaded: false,
      squareError: null,
      createPayment: vi.fn(),
      isCreatingPayment: false,
      paymentError: null,
      checkPaymentStatus: vi.fn(),
      refundPayment: vi.fn(),
    }));

    vi.mock("@/common/api/hooks/useSquarePayment", () => ({
      useSquarePayment: mockUseSquarePayment,
    }));

    render(
      <QueryClientProvider client={queryClient}>
        <SquarePaymentForm {...defaultProps} />
      </QueryClientProvider>
    );

    // Vérifier que le message de chargement est affiché
    expect(
      screen.queryByText(/chargement du module de paiement/i)
    ).toBeInTheDocument();
  });

  it("devrait afficher le montant à payer", async () => {
    render(<SquarePaymentForm {...defaultProps} />, { wrapper });

    await waitFor(() => {
      expect(screen.getByText("99.99 $")).toBeInTheDocument();
    });
  });

  it("devrait afficher les options de méthode de paiement", async () => {
    render(<SquarePaymentForm {...defaultProps} />, { wrapper });

    await waitFor(() => {
      expect(screen.getByText(/carte/i)).toBeInTheDocument();
      expect(screen.getByText(/google pay/i)).toBeInTheDocument();
      expect(screen.getByText(/apple pay/i)).toBeInTheDocument();
      expect(screen.getByText(/cash app/i)).toBeInTheDocument();
    });
  });

  it("devrait sélectionner la méthode de paiement par carte par défaut", async () => {
    render(<SquarePaymentForm {...defaultProps} />, { wrapper });

    await waitFor(() => {
      const carteButton = screen.getByRole("button", { name: /carte/i });
      expect(carteButton).toHaveClass("bg-default"); // ou toute autre classe active
    });
  });

  it("devrait permettre de changer de méthode de paiement", async () => {
    render(<SquarePaymentForm {...defaultProps} />, { wrapper });

    await waitFor(() => {
      const googlePayButton = screen.getByRole("button", {
        name: /google pay/i,
      });
      fireEvent.click(googlePayButton);
    });

    // Vérifier que le bouton Google Pay est maintenant actif
    const googlePayButton = screen.getByRole("button", { name: /google pay/i });
    expect(googlePayButton).toHaveClass("bg-default");
  });

  it("devrait afficher le bouton de paiement avec le montant", async () => {
    render(<SquarePaymentForm {...defaultProps} />, { wrapper });

    await waitFor(() => {
      expect(screen.getByText(/payer 99.99 \$/i)).toBeInTheDocument();
    });
  });

  it("devrait afficher les informations de sécurité", async () => {
    render(<SquarePaymentForm {...defaultProps} />, { wrapper });

    await waitFor(() => {
      expect(screen.getByText(/paiement sécurisé par square/i)).toBeInTheDocument();
      expect(
        screen.getByText(/vos informations de paiement ne sont jamais stockées/i)
      ).toBeInTheDocument();
    });
  });

  it("devrait afficher une erreur si Square n'est pas chargé", () => {
    const mockUseSquarePayment = vi.fn(() => ({
      isSquareLoaded: true,
      squareError: "Erreur de chargement Square",
      createPayment: vi.fn(),
      isCreatingPayment: false,
      paymentError: null,
      checkPaymentStatus: vi.fn(),
      refundPayment: vi.fn(),
    }));

    vi.mock("@/common/api/hooks/useSquarePayment", () => ({
      useSquarePayment: mockUseSquarePayment,
    }));

    render(
      <QueryClientProvider client={queryClient}>
        <SquarePaymentForm {...defaultProps} />
      </QueryClientProvider>
    );

    expect(screen.getByText(/erreur de chargement square/i)).toBeInTheDocument();
  });

  it("devrait désactiver le bouton de paiement pendant le traitement", async () => {
    const mockUseSquarePayment = vi.fn(() => ({
      isSquareLoaded: true,
      squareError: null,
      createPayment: vi.fn(),
      isCreatingPayment: true,
      paymentError: null,
      checkPaymentStatus: vi.fn(),
      refundPayment: vi.fn(),
    }));

    vi.mock("@/common/api/hooks/useSquarePayment", () => ({
      useSquarePayment: mockUseSquarePayment,
    }));

    render(
      <QueryClientProvider client={queryClient}>
        <SquarePaymentForm {...defaultProps} />
      </QueryClientProvider>
    );

    await waitFor(() => {
      const payButton = screen.getByRole("button", { name: /traitement/i });
      expect(payButton).toBeDisabled();
    });
  });

  it("devrait afficher le titre du formulaire", async () => {
    render(<SquarePaymentForm {...defaultProps} />, { wrapper });

    await waitFor(() => {
      expect(screen.getByText(/paiement sécurisé via square/i)).toBeInTheDocument();
    });
  });
});

