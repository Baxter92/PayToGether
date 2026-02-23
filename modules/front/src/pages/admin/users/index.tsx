import { useMemo, useState, type ReactElement } from "react";
import type { ColumnDef } from "@tanstack/react-table";
import { Plus, X, Pencil, KeyRound, Shield, Trash2 } from "lucide-react";
import {
  RoleUtilisateur,
  StatutUtilisateur,
  type CreateUtilisateurDTO,
  type RoleUtilisateurType,
  type StatutUtilisateurType,
  type UtilisateurDTO,
  useAssignUserRole,
  useCreateUser,
  useDeleteUser,
  useResetUserPassword,
  useSetUserEnabled,
  useUpdateUser,
  useUsers,
} from "@/common/api";
import { Button } from "@/common/components/ui/button";
import { Card, CardContent } from "@/common/components/ui/card";
import { Badge } from "@/common/components/ui/badge";
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/common/components/ui/avatar";
import { Switch } from "@/common/components/ui/switch";
import { DataTable } from "@/common/components";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/common/components/ui/dialog";
import Form from "@/common/containers/Form";
import { toast } from "sonner";
import { createUtilisateurSchema } from "@/common/schemas/utilisateur.schema";
import { useI18n } from "@/common/hooks/useI18n";

type UserRow = {
  uuid: string;
  nom: string;
  prenom: string;
  fullName: string;
  email: string;
  photoProfil?: string | null;
  statut: StatutUtilisateurType;
  role: RoleUtilisateurType;
  dateCreation?: string;
};

type CreateUserFormValues = CreateUtilisateurDTO;

type UpdateUserFormValues = {
  nom?: string;
  prenom?: string;
  email: string;
  motDePasse: string;
  photoProfil?: string;
};

type ResetPasswordFormValues = {
  nouveauMotDePasse: string;
};

const roleItems = [
  { label: "Utilisateur", value: RoleUtilisateur.UTILISATEUR },
  { label: "Vendeur", value: RoleUtilisateur.VENDEUR },
  { label: "Admin", value: RoleUtilisateur.ADMIN },
];

export default function AdminUsers(): ReactElement {
  const [openCreateModal, setOpenCreateModal] = useState(false);
  const [openEditModal, setOpenEditModal] = useState(false);
  const [openResetPasswordModal, setOpenResetPasswordModal] = useState(false);
  const [openAssignRoleModal, setOpenAssignRoleModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState<UserRow | null>(null);
  const [roleToAssign, setRoleToAssign] = useState<RoleUtilisateurType>(
    RoleUtilisateur.UTILISATEUR,
  );

  const { t: tAdmin } = useI18n("admin");
  const { t: tRoles } = useI18n("roles");
  const { t: tStatus } = useI18n("status");
  const { data: usersData, isLoading, error, refetch } = useUsers();
  const { mutateAsync: createUser, isPending: isCreating } = useCreateUser();
  const { mutateAsync: updateUser, isPending: isUpdating } = useUpdateUser();
  const { mutateAsync: deleteUser, isPending: isDeleting } = useDeleteUser();
  const { mutateAsync: setUserEnabled, isPending: isSettingEnabled } =
    useSetUserEnabled();
  const { mutateAsync: assignUserRole, isPending: isAssigningRole } =
    useAssignUserRole();
  const { mutateAsync: resetUserPassword, isPending: isResettingPassword } =
    useResetUserPassword();

  const users = useMemo<UserRow[]>(
    () =>
      (usersData ?? []).map((user: UtilisateurDTO) => ({
        uuid: user.uuid,
        nom: user.nom,
        prenom: user.prenom,
        fullName: [user.prenom, user.nom].filter(Boolean).join(" ").trim(),
        email: user.email,
        photoProfil: user.photoProfil || "",
        statut: user.statut,
        role: user.role,
        dateCreation: user.dateCreation,
      })),
    [usersData],
  );

  const getRoleBadge = (role: RoleUtilisateurType) => {
    switch (role) {
      case RoleUtilisateur.ADMIN:
        return (
          <Badge className="bg-primary/10 text-primary hover:bg-primary/10">
            {tRoles("admin")}
          </Badge>
        );
      case RoleUtilisateur.VENDEUR:
        return <Badge variant="outline">{tRoles("seller")}</Badge>;
      default:
        return <Badge variant="outline">{tRoles("user")}</Badge>;
    }
  };

  const getStatusBadge = (statut: StatutUtilisateurType) => {
    if (statut === StatutUtilisateur.ACTIF) {
      return (
        <Badge className="bg-green-100 text-green-800 hover:bg-green-100">
          {tStatus("active")}
        </Badge>
      );
    }

    return (
      <Badge className="bg-destructive/10 text-destructive hover:bg-destructive/10">
        {tStatus("banned")}
      </Badge>
    );
  };

  const handleCreateUser = async (data: CreateUserFormValues) => {
    try {
      await createUser(data);
      toast.success("Utilisateur créé", {
        description: `${data.prenom} ${data.nom} a été créé avec succès`,
      });
      setOpenCreateModal(false);
    } catch (err: any) {
      toast.error("Erreur lors de la création", {
        description: err?.message ?? "Une erreur est survenue",
      });
    }
  };

  const handleUpdateUser = async (data: UpdateUserFormValues) => {
    if (!selectedUser) return;

    try {
      await updateUser({
        id: selectedUser.uuid,
        data: {
          nom: data.nom ?? "",
          prenom: data.prenom ?? "",
          email: data.email,
          motDePasse: data.motDePasse,
          statut: selectedUser.statut,
          role: selectedUser.role,
          photoProfil: data.photoProfil,
        },
      });

      toast.success("Utilisateur mis à jour", {
        description: `${selectedUser.fullName} a été mis à jour`,
      });
      setOpenEditModal(false);
      setSelectedUser(null);
    } catch (err: any) {
      toast.error("Erreur lors de la mise à jour", {
        description: err?.message ?? "Une erreur est survenue",
      });
    }
  };

  const handleResetPassword = async (data: ResetPasswordFormValues) => {
    if (!selectedUser) return;

    try {
      await resetUserPassword({
        utilisateurUuid: selectedUser.uuid,
        data: { nouveauMotDePasse: data.nouveauMotDePasse },
      });
      toast.success("Mot de passe réinitialisé");
      setOpenResetPasswordModal(false);
      setSelectedUser(null);
    } catch (err: any) {
      toast.error("Erreur lors de la réinitialisation", {
        description: err?.message ?? "Une erreur est survenue",
      });
    }
  };

  const handleToggleEnabled = async (user: UserRow, checked: boolean) => {
    try {
      await setUserEnabled({
        utilisateurUuid: user.uuid,
        data: { actif: checked },
      });
      toast.success(checked ? "Utilisateur activé" : "Utilisateur désactivé", {
        description: user.fullName,
      });
    } catch (err: any) {
      toast.error("Erreur lors du changement de statut", {
        description: err?.message ?? "Une erreur est survenue",
      });
    }
  };

  const handleAssignRole = async (user: UserRow, value: string) => {
    if (user.role === value) return;

    try {
      await assignUserRole({
        utilisateurUuid: user.uuid,
        data: { nomRole: value as RoleUtilisateurType },
      });
      toast.success("Rôle mis à jour", {
        description: `${user.fullName} est maintenant ${value}`,
      });
    } catch (err: any) {
      toast.error("Erreur lors du changement de rôle", {
        description: err?.message ?? "Une erreur est survenue",
      });
    }
  };

  const handleAssignRoleFromModal = async () => {
    if (!selectedUser) return;
    await handleAssignRole(selectedUser, roleToAssign);
    setOpenAssignRoleModal(false);
    setSelectedUser(null);
  };

  const handleDeleteUser = async (user: UserRow) => {
    const ok = window.confirm(
      `Supprimer l'utilisateur ${user.fullName} (${user.email}) ?`,
    );
    if (!ok) return;

    try {
      await deleteUser(user.uuid);
      toast.success("Utilisateur supprimé", { description: user.fullName });
    } catch (err: any) {
      toast.error("Erreur lors de la suppression", {
        description: err?.message ?? "Une erreur est survenue",
      });
    }
  };

  const isActionsPending =
    isUpdating ||
    isDeleting ||
    isSettingEnabled ||
    isAssigningRole ||
    isResettingPassword;

  const columns = useMemo<ColumnDef<UserRow>[]>(
    () => [
      {
        id: "fullName",
        header: tAdmin("users.name"),
        accessorKey: "fullName",
        cell: ({ row }) => {
          const user = row.original;
          return (
            <div className="flex items-center gap-3">
              <Avatar className="h-8 w-8">
                <AvatarImage src={user.photoProfil || ""} />
                <AvatarFallback>
                  {user.fullName
                    .split(" ")
                    .filter(Boolean)
                    .map((n) => n[0])
                    .join("")
                    .slice(0, 2)}
                </AvatarFallback>
              </Avatar>
              <div>
                <p className="font-medium">{user.fullName || "-"}</p>
                <p className="text-sm text-muted-foreground">{user.email}</p>
              </div>
            </div>
          );
        },
      },
      {
        id: "role",
        header: tAdmin("users.role"),
        accessorKey: "role",
        cell: ({ row }) => getRoleBadge(row.original.role),
      },
      {
        id: "enabled",
        header: "Enable",
        accessorKey: "statut",
        enableSorting: false,
        cell: ({ row }) => {
          const user = row.original;
          const checked = user.statut === StatutUtilisateur.ACTIF;
          return (
            <Switch
              checked={checked}
              onCheckedChange={(value) => {
                void handleToggleEnabled(user, value);
              }}
              disabled={isActionsPending}
            />
          );
        },
      },
      {
        id: "status",
        header: tAdmin("users.status"),
        accessorKey: "statut",
        cell: ({ row }) => getStatusBadge(row.original.statut),
      },
    ],
    [tAdmin, isActionsPending, tRoles, tStatus],
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold font-heading">
            {tAdmin("users.title")}
          </h1>
          <p className="text-muted-foreground">{tAdmin("users.description")}</p>
        </div>
        <Button
          leftIcon={<Plus className="h-4 w-4" />}
          onClick={() => setOpenCreateModal(true)}
        >
          Nouvel utilisateur
        </Button>
      </div>

      <Card>
        <CardContent className="pt-6">
          {isLoading && (
            <div className="text-center py-8 text-muted-foreground">
              Chargement...
            </div>
          )}
          {error && (
            <div className="text-center py-8 text-destructive">
              {error.message}
            </div>
          )}
          {!isLoading && !error && (
            <DataTable<UserRow, unknown>
              columns={columns}
              data={users}
              searchKey={["fullName", "email"]}
              searchPlaceholder={tAdmin("users.search")}
              enableSelection={false}
              enableRowNumber
              enableExport
              enableSorting
              onRefresh={() => {
                void refetch();
              }}
              actionsRow={({ row }) => {
                const user = row.original;
                return [
                  {
                    leftIcon: <Shield className="h-4 w-4" />,
                    tooltip: "Assigner rôle",
                    disabled: isActionsPending,
                    onClick: () => {
                      setSelectedUser(user);
                      setRoleToAssign(user.role);
                      setOpenAssignRoleModal(true);
                    },
                  },
                  {
                    leftIcon: <Pencil className="h-4 w-4" />,
                    tooltip: "Modifier",
                    disabled: isActionsPending,
                    onClick: () => {
                      setSelectedUser(user);
                      setOpenEditModal(true);
                    },
                  },
                  {
                    leftIcon: <KeyRound className="h-4 w-4" />,
                    tooltip: "Réinitialiser mot de passe",
                    disabled: isActionsPending,
                    onClick: () => {
                      setSelectedUser(user);
                      setOpenResetPasswordModal(true);
                    },
                  },
                  {
                    leftIcon: <Trash2 className="h-4 w-4" />,
                    tooltip: "Supprimer",
                    colorScheme: "danger",
                    disabled: isActionsPending,
                    onClick: () => {
                      void handleDeleteUser(user);
                    },
                  },
                ];
              }}
            />
          )}
        </CardContent>
      </Card>

      <Dialog open={openCreateModal} onOpenChange={setOpenCreateModal}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Créer un utilisateur</DialogTitle>
            <DialogDescription>
              Renseignez les informations du nouvel utilisateur.
            </DialogDescription>
          </DialogHeader>

          <Form<CreateUserFormValues>
            schema={createUtilisateurSchema}
            defaultValues={{
              nom: "",
              prenom: "",
              email: "",
              motDePasse: "",
              photoProfil: "",
              statut: StatutUtilisateur.ACTIF,
              role: RoleUtilisateur.UTILISATEUR,
            }}
            onSubmit={async ({ data }) => {
              await handleCreateUser(data);
            }}
            submitLabel="Créer"
            resetLabel="Annuler"
            isLoading={isCreating}
            submitBtnProps={{
              leftIcon: <Plus className="h-4 w-4" />,
            }}
            resetBtnProps={{
              leftIcon: <X className="h-4 w-4" />,
              onClick: () => setOpenCreateModal(false),
            }}
            fields={[
              {
                name: "nom",
                label: "Nom",
                type: "text",
                placeholder: "Ex: Dupont",
              },
              {
                name: "prenom",
                label: "Prénom",
                type: "text",
                placeholder: "Ex: Jean",
              },
              {
                name: "email",
                label: "Email",
                type: "email",
                placeholder: "jean.dupont@email.com",
              },
              {
                name: "motDePasse",
                label: "Mot de passe",
                type: "password",
                placeholder: "********",
              },
              {
                name: "statut",
                label: "Statut",
                type: "select",
                items: [
                  { label: "Actif", value: StatutUtilisateur.ACTIF },
                  { label: "Inactif", value: StatutUtilisateur.INACTIF },
                ],
              },
              {
                name: "role",
                label: "Rôle",
                type: "select",
                items: roleItems,
              },
            ]}
          />
        </DialogContent>
      </Dialog>

      <Dialog open={openEditModal} onOpenChange={setOpenEditModal}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Modifier un utilisateur</DialogTitle>
            <DialogDescription>
              Mettre à jour les informations de l'utilisateur.
            </DialogDescription>
          </DialogHeader>

          {selectedUser && (
            <Form<UpdateUserFormValues>
              defaultValues={{
                nom: selectedUser.nom,
                prenom: selectedUser.prenom,
                email: selectedUser.email,
                motDePasse: "",
                photoProfil: selectedUser.photoProfil || "",
              }}
              onSubmit={async ({ data }) => {
                await handleUpdateUser(data);
              }}
              submitLabel="Mettre à jour"
              resetLabel="Annuler"
              isLoading={isUpdating}
              submitBtnProps={{
                leftIcon: <Pencil className="h-4 w-4" />,
              }}
              resetBtnProps={{
                leftIcon: <X className="h-4 w-4" />,
                onClick: () => {
                  setOpenEditModal(false);
                  setSelectedUser(null);
                },
              }}
              fields={[
                { name: "nom", label: "Nom", type: "text" },
                { name: "prenom", label: "Prénom", type: "text" },
                { name: "email", label: "Email", type: "email" },
                {
                  name: "motDePasse",
                  label: "Mot de passe (requis par le backend)",
                  type: "password",
                },
                {
                  name: "photoProfil",
                  label: "Photo profil (URL)",
                  type: "text",
                },
              ]}
            />
          )}
        </DialogContent>
      </Dialog>

      <Dialog
        open={openResetPasswordModal}
        onOpenChange={setOpenResetPasswordModal}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Réinitialiser le mot de passe</DialogTitle>
            <DialogDescription>
              Définir un nouveau mot de passe pour {selectedUser?.fullName}.
            </DialogDescription>
          </DialogHeader>

          {selectedUser && (
            <Form<ResetPasswordFormValues>
              defaultValues={{ nouveauMotDePasse: "" }}
              onSubmit={async ({ data }) => {
                await handleResetPassword(data);
              }}
              submitLabel="Réinitialiser"
              resetLabel="Annuler"
              isLoading={isResettingPassword}
              submitBtnProps={{
                leftIcon: <KeyRound className="h-4 w-4" />,
              }}
              resetBtnProps={{
                leftIcon: <X className="h-4 w-4" />,
                onClick: () => {
                  setOpenResetPasswordModal(false);
                  setSelectedUser(null);
                },
              }}
              fields={[
                {
                  name: "nouveauMotDePasse",
                  label: "Nouveau mot de passe",
                  type: "password",
                  placeholder: "********",
                },
              ]}
            />
          )}
        </DialogContent>
      </Dialog>

      <Dialog open={openAssignRoleModal} onOpenChange={setOpenAssignRoleModal}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Assigner un rôle</DialogTitle>
            <DialogDescription>
              Choisissez le rôle pour {selectedUser?.fullName}.
            </DialogDescription>
          </DialogHeader>

          {selectedUser && (
            <Form<{ role: RoleUtilisateurType }>
              defaultValues={{ role: roleToAssign }}
              onSubmit={async () => {
                await handleAssignRoleFromModal();
              }}
              submitLabel="Assigner"
              resetLabel="Annuler"
              isLoading={isAssigningRole}
              submitBtnProps={{
                leftIcon: <Shield className="h-4 w-4" />,
              }}
              resetBtnProps={{
                leftIcon: <X className="h-4 w-4" />,
                onClick: () => {
                  setOpenAssignRoleModal(false);
                  setSelectedUser(null);
                },
              }}
              fields={[
                {
                  name: "role",
                  label: "Rôle",
                  type: "select",
                  items: roleItems,
                  value: roleToAssign,
                  onValueChange: (value) => {
                    setRoleToAssign(value as RoleUtilisateurType);
                  },
                },
              ]}
            />
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
