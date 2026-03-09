import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  paymentService,
  paymentAdminService,
} from "../services/paymentService";
import type {
  PaymentDTO,
  CreateSquarePaymentDTO,
  SquarePaymentResponseDTO,
  PaiementListResponseDTO,
  StatutPaiementType,
  MethodePaiementType,
} from "../types";
import { createResourceHooks } from "./factories/createResourceHooks";

const paymentHooks = createResourceHooks<
  PaymentDTO,
  any, // Pas de création directe de paiement, seulement via Square
  Partial<PaymentDTO>
>({
  resourceName: "paiements",
  service: paymentService,
  customKeys: {
    byStatut: (statut: StatutPaiementType) =>
      ["paiements", "statut", statut] as const,
    byCommande: (commandeUuid: string) =>
      ["paiements", "commande", commandeUuid] as const,
    byClient: (clientUuid: string) =>
      ["paiements", "client", clientUuid] as const,
    byMarchand: (marchandUuid: string) =>
      ["paiements", "marchand", marchandUuid] as const,
  },
});

export const {
  keys: paymentKeys,
  useList: usePayments,
  useDetail: usePayment,
  useDelete: useDeletePayment,
} = paymentHooks;

// Hook pour créer un paiement Square
export const useCreateSquarePayment = () => {
  const queryClient = useQueryClient();

  return useMutation<SquarePaymentResponseDTO, Error, CreateSquarePaymentDTO>({
    mutationFn: (paymentData) =>
      paymentService.createSquarePayment(paymentData),
    onSuccess: (newPayment) => {
      queryClient.invalidateQueries({ queryKey: paymentKeys.lists() });
      queryClient.invalidateQueries({
        queryKey: paymentKeys.byCommande(newPayment.commandeUuid),
      });
      queryClient.invalidateQueries({
        queryKey: paymentKeys.byClient(newPayment.utilisateurUuid),
      });
    },
  });
};

// Hook pour vérifier le statut d'un paiement Square
export const useCheckSquarePaymentStatus = () => {
  const queryClient = useQueryClient();

  return useMutation<SquarePaymentResponseDTO, Error, string>({
    mutationFn: (paiementUuid) =>
      paymentService.checkSquarePaymentStatus(paiementUuid),
    onSuccess: (updatedPayment) => {
      queryClient.invalidateQueries({
        queryKey: paymentKeys.detail(updatedPayment.uuid),
      });
      queryClient.invalidateQueries({
        queryKey: paymentKeys.byCommande(updatedPayment.commandeUuid),
      });
    },
  });
};

// Hook pour rembourser un paiement Square
export const useRefundSquarePayment = () => {
  const queryClient = useQueryClient();

  return useMutation<SquarePaymentResponseDTO, Error, string>({
    mutationFn: (paiementUuid) =>
      paymentService.refundSquarePayment(paiementUuid),
    onSuccess: (refundedPayment) => {
      queryClient.invalidateQueries({
        queryKey: paymentKeys.detail(refundedPayment.uuid),
      });
      queryClient.invalidateQueries({
        queryKey: paymentKeys.byCommande(refundedPayment.commandeUuid),
      });
      queryClient.invalidateQueries({ queryKey: paymentKeys.lists() });
    },
  });
};

// Hook pour récupérer les paiements par statut
export const usePaymentsByStatut = (statut: StatutPaiementType) => {
  return useQuery<PaymentDTO[], Error>({
    queryKey: paymentKeys.byStatut(statut),
    queryFn: () => paymentService.getByStatut(statut),
    enabled: !!statut,
  });
};

// Hook pour récupérer les paiements d'une commande
export const usePaymentsByCommande = (commandeUuid: string) => {
  return useQuery<PaymentDTO[], Error>({
    queryKey: paymentKeys.byCommande(commandeUuid),
    queryFn: () => paymentService.getByCommande(commandeUuid),
    enabled: !!commandeUuid,
  });
};

// Hook pour récupérer les paiements d'un client
export const usePaymentsByClient = (clientUuid: string) => {
  return useQuery<PaymentDTO[], Error>({
    queryKey: paymentKeys.byClient(clientUuid),
    queryFn: () => paymentService.getByClient(clientUuid),
    enabled: !!clientUuid,
  });
};

// Hook pour récupérer les paiements d'un marchand
export const usePaymentsByMarchand = (marchandUuid: string) => {
  return useQuery<PaymentDTO[], Error>({
    queryKey: paymentKeys.byMarchand(marchandUuid),
    queryFn: () => paymentService.getByMarchand(marchandUuid),
    enabled: !!marchandUuid,
  });
};

// ===== HOOKS ADMIN =====

// Hook pour lister tous les paiements (admin)
export const useAdminPayments = () => {
  return useQuery<PaiementListResponseDTO, Error>({
    queryKey: ["admin", "paiements"],
    queryFn: () => paymentAdminService.listAll(),
  });
};

// Hook pour récupérer uniquement les statistiques des paiements (admin)
export const useAdminPaymentStats = () => {
  return useQuery<any, Error>({
    queryKey: ["admin", "paiements", "statistiques"],
    queryFn: () => paymentAdminService.getStats(),
  });
};

// Hook pour récupérer les paiements par statut (admin)
export const useAdminPaymentsByStatut = (statut: StatutPaiementType) => {
  return useQuery<PaymentDTO[], Error>({
    queryKey: ["admin", "paiements", "statut", statut],
    queryFn: () => paymentAdminService.getByStatut(statut),
    enabled: !!statut,
  });
};

// Hook pour récupérer les paiements par méthode (admin)
export const useAdminPaymentsByMethode = (methode: MethodePaiementType) => {
  return useQuery<PaymentDTO[], Error>({
    queryKey: ["admin", "paiements", "methode", methode],
    queryFn: () => paymentAdminService.getByMethode(methode),
    enabled: !!methode,
  });
};
