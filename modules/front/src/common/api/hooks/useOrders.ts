import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { orderService, orderAdminService } from "../services/orderService";
import type {
  OrderDTO,
  CreateOrderDTO,
  UpdateOrderDTO,
  StatutCommandeType,
  OrderListResponseDTO,
  CommandeListDTO,
  CommandeUtilisateurDTO,
  ValidationFacturesClientResponseDTO,
} from "../types";
import { createResourceHooks } from "./factories/createResourceHooks";

const orderHooks = createResourceHooks<
  OrderDTO,
  CreateOrderDTO,
  UpdateOrderDTO
>({
  resourceName: "commandes",
  service: orderService,
  customKeys: {
    myOrders: () => ["commandes", "me"] as const,
    byUtilisateur: (utilisateurUuid: string) =>
      ["commandes", "utilisateur", utilisateurUuid] as const,
    byDeal: (dealUuid: string) => ["commandes", "deal", dealUuid] as const,
    byStatut: (statut: StatutCommandeType) =>
      ["commandes", "statut", statut] as const,
  },
});

export const {
  keys: orderKeys,
  useList: useOrders,
  useDetail: useOrder,
  useCreate: useCreateOrder,
  useUpdate: useUpdateOrder,
  useDelete: useDeleteOrder,
} = orderHooks;

// Hook pour récupérer mes commandes
export const useMyOrders = () => {
  return useQuery<OrderDTO[], Error>({
    queryKey: orderKeys.myOrders(),
    queryFn: () => orderService.getMyOrders(),
  });
};

// Hook pour récupérer les commandes d'un utilisateur
export const useOrdersByUtilisateur = (utilisateurUuid: string) => {
  return useQuery<OrderDTO[], Error>({
    queryKey: orderKeys.byUtilisateur(utilisateurUuid),
    queryFn: () => orderService.getByUtilisateur(utilisateurUuid),
    enabled: !!utilisateurUuid,
  });
};

// Hook pour récupérer les commandes d'un deal
export const useOrdersByDeal = (dealUuid: string) => {
  return useQuery<OrderDTO[], Error>({
    queryKey: orderKeys.byDeal(dealUuid),
    queryFn: () => orderService.getByDeal(dealUuid),
    enabled: !!dealUuid,
  });
};

// Hook pour récupérer les commandes par statut
export const useOrdersByStatut = (statut: StatutCommandeType) => {
  return useQuery<OrderDTO[], Error>({
    queryKey: orderKeys.byStatut(statut),
    queryFn: () => orderService.getByStatut(statut),
    enabled: !!statut,
  });
};

// Hook pour mettre à jour le statut d'une commande
export const useUpdateOrderStatus = () => {
  const queryClient = useQueryClient();

  return useMutation<
    OrderDTO,
    Error,
    { uuid: string; statut: StatutCommandeType }
  >({
    mutationFn: ({ uuid, statut }) => orderService.updateStatus(uuid, statut),
    onSuccess: (updatedOrder) => {
      queryClient.invalidateQueries({ queryKey: orderKeys.lists() });
      queryClient.invalidateQueries({
        queryKey: orderKeys.detail(updatedOrder.uuid),
      });
      queryClient.invalidateQueries({
        queryKey: orderKeys.byUtilisateur(updatedOrder.utilisateurUuid),
      });
      queryClient.invalidateQueries({
        queryKey: orderKeys.byDeal(updatedOrder.dealUuid),
      });
      queryClient.invalidateQueries({
        queryKey: orderKeys.myOrders(),
      });
    },
  });
};

// Hook pour annuler une commande
export const useCancelOrder = () => {
  const queryClient = useQueryClient();

  return useMutation<OrderDTO, Error, string>({
    mutationFn: (uuid) => orderService.cancel(uuid),
    onSuccess: (cancelledOrder) => {
      queryClient.invalidateQueries({ queryKey: orderKeys.lists() });
      queryClient.invalidateQueries({
        queryKey: orderKeys.detail(cancelledOrder.uuid),
      });
      queryClient.invalidateQueries({
        queryKey: orderKeys.byUtilisateur(cancelledOrder.utilisateurUuid),
      });
      queryClient.invalidateQueries({
        queryKey: orderKeys.byDeal(cancelledOrder.dealUuid),
      });
      queryClient.invalidateQueries({
        queryKey: orderKeys.myOrders(),
      });
    },
  });
};

// Hook pour marquer une commande comme livrée
export const useMarkOrderAsDelivered = () => {
  const queryClient = useQueryClient();

  return useMutation<OrderDTO, Error, string>({
    mutationFn: (uuid) => orderService.markAsDelivered(uuid),
    onSuccess: (deliveredOrder) => {
      queryClient.invalidateQueries({ queryKey: orderKeys.lists() });
      queryClient.invalidateQueries({
        queryKey: orderKeys.detail(deliveredOrder.uuid),
      });
      queryClient.invalidateQueries({
        queryKey: orderKeys.byUtilisateur(deliveredOrder.utilisateurUuid),
      });
      queryClient.invalidateQueries({
        queryKey: orderKeys.byDeal(deliveredOrder.dealUuid),
      });
      queryClient.invalidateQueries({
        queryKey: orderKeys.myOrders(),
      });
    },
  });
};

// ===== HOOKS ADMIN =====

// Hook pour lister toutes les commandes (admin)
export const useAdminOrders = () => {
  return useQuery<OrderListResponseDTO, Error>({
    queryKey: ["admin", "commandes"],
    queryFn: () => orderAdminService.listAll(),
  });
};

// Hook pour lister les commandes d'un marchand
export const useMerchantOrders = (marchandUuid: string) => {
  return useQuery<OrderListResponseDTO, Error>({
    queryKey: ["admin", "commandes", "marchand", marchandUuid],
    queryFn: () => orderAdminService.listByMerchant(marchandUuid),
    enabled: !!marchandUuid,
  });
};

// Hook pour récupérer les commandes par statut (admin)
export const useAdminOrdersByStatut = (statut: StatutCommandeType) => {
  return useQuery<OrderDTO[], Error>({
    queryKey: ["admin", "commandes", "statut", statut],
    queryFn: () => orderAdminService.getByStatut(statut),
    enabled: !!statut,
  });
};

// Hook pour récupérer les commandes par utilisateur (admin)
export const useAdminOrdersByUtilisateur = (utilisateurUuid: string) => {
  return useQuery<OrderDTO[], Error>({
    queryKey: ["admin", "commandes", "utilisateur", utilisateurUuid],
    queryFn: () => orderAdminService.getByUtilisateur(utilisateurUuid),
    enabled: !!utilisateurUuid,
  });
};

// Hook pour récupérer les commandes par deal (admin)
export const useAdminOrdersByDeal = (dealUuid: string) => {
  return useQuery<OrderDTO[], Error>({
    queryKey: ["admin", "commandes", "deal", dealUuid],
    queryFn: () => orderAdminService.getByDeal(dealUuid),
    enabled: !!dealUuid,
  });
};

// Hook pour mettre à jour le statut d'une commande (admin)
export const useAdminUpdateOrderStatus = () => {
  const queryClient = useQueryClient();

  return useMutation<
    OrderDTO,
    Error,
    { uuid: string; statut: StatutCommandeType }
  >({
    mutationFn: ({ uuid, statut }) =>
      orderAdminService.updateStatus(uuid, statut),
    onSuccess: (updatedOrder) => {
      queryClient.invalidateQueries({ queryKey: ["admin", "commandes"] });
      queryClient.invalidateQueries({
        queryKey: orderKeys.detail(updatedOrder.uuid),
      });
      queryClient.invalidateQueries({
        queryKey: [
          "admin",
          "commandes",
          "utilisateur",
          updatedOrder.utilisateurUuid,
        ],
      });
      queryClient.invalidateQueries({
        queryKey: ["admin", "commandes", "deal", updatedOrder.dealUuid],
      });
    },
  });
};

// Hook pour annuler une commande (admin)
export const useAdminCancelOrder = () => {
  const queryClient = useQueryClient();

  return useMutation<OrderDTO, Error, { uuid: string; raison?: string }>({
    mutationFn: ({ uuid, raison }) => orderAdminService.cancel(uuid, raison),
    onSuccess: (cancelledOrder) => {
      queryClient.invalidateQueries({ queryKey: ["admin", "commandes"] });
      queryClient.invalidateQueries({
        queryKey: orderKeys.detail(cancelledOrder.uuid),
      });
      queryClient.invalidateQueries({
        queryKey: [
          "admin",
          "commandes",
          "utilisateur",
          cancelledOrder.utilisateurUuid,
        ],
      });
      queryClient.invalidateQueries({
        queryKey: ["admin", "commandes", "deal", cancelledOrder.dealUuid],
      });
    },
  });
};

// Hook pour supprimer une commande (admin)
export const useAdminDeleteOrder = () => {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: (uuid) => orderAdminService.delete(uuid),
    onSuccess: (_, uuid) => {
      queryClient.invalidateQueries({ queryKey: ["admin", "commandes"] });
      queryClient.invalidateQueries({
        queryKey: orderKeys.detail(uuid),
      });
    },
  });
};

// Hook pour valider le payout (admin) - retourne CommandeListDTO
export const useAdminValidatePayout = () => {
  const queryClient = useQueryClient();

  return useMutation<
    CommandeListDTO,
    Error,
    { uuid: string; dateDepotPayout: string }
  >({
    mutationFn: ({ uuid, dateDepotPayout }) =>
      orderAdminService.validatePayout(uuid, dateDepotPayout),
    onSuccess: (updatedOrder) => {
      queryClient.invalidateQueries({ queryKey: ["admin", "commandes"] });
      queryClient.invalidateQueries({
        queryKey: orderKeys.detail(updatedOrder.uuid),
      });
    },
  });
};

// Hook pour uploader la facture du vendeur - retourne CommandeListDTO
export const useAdminUploadSellerInvoice = () => {
  const queryClient = useQueryClient();

  return useMutation<CommandeListDTO, Error, { uuid: string; file: File }>({
    mutationFn: ({ uuid, file }) =>
      orderAdminService.uploadSellerInvoice(uuid, file),
    onSuccess: (updatedOrder) => {
      queryClient.invalidateQueries({ queryKey: ["admin", "commandes"] });
      queryClient.invalidateQueries({
        queryKey: orderKeys.detail(updatedOrder.uuid),
      });
    },
  });
};

// Hook pour récupérer les clients d'une commande
export const useOrderCustomers = (uuid: string) => {
  return useQuery<CommandeUtilisateurDTO[], Error>({
    queryKey: ["admin", "commandes", uuid, "customers"],
    queryFn: () => orderAdminService.getOrderCustomers(uuid),
    enabled: !!uuid,
  });
};

// Hook pour valider les factures clients
// ✅ Envoie { utilisateurUuids: [...] } et retourne ValidationFacturesClientResponseDTO
export const useAdminValidateCustomerInvoices = () => {
  const queryClient = useQueryClient();

  return useMutation<
    ValidationFacturesClientResponseDTO,
    Error,
    { uuid: string; utilisateurUuids: string[] }
  >({
    mutationFn: ({ uuid, utilisateurUuids }) =>
      orderAdminService.validateCustomerInvoices(uuid, utilisateurUuids),
    onSuccess: (_result, variables) => {
      queryClient.invalidateQueries({ queryKey: ["admin", "commandes"] });
      queryClient.invalidateQueries({
        queryKey: orderKeys.detail(variables.uuid),
      });
      queryClient.invalidateQueries({
        queryKey: ["admin", "commandes", variables.uuid, "customers"],
      });
    },
  });
};

