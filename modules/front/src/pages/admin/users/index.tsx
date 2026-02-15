import type { ReactElement } from "react";
import { useState } from "react";
import {
  Search,
  MoreHorizontal,
  Mail,
  Ban,
  Shield,
  Plus,
  X,
} from "lucide-react";
import {
  RoleUtilisateur,
  StatutUtilisateur,
  type CreateUtilisateurDTO,
  useCreateUser,
  useUsers,
} from "@/common/api";
import { Button } from "@/common/components/ui/button";
import { Input } from "@/common/components/ui/input";
import { Card, CardContent, CardHeader } from "@/common/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/common/components/ui/table";
import { Badge } from "@/common/components/ui/badge";
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from "@/common/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/common/components/ui/dropdown-menu";
import { formatCurrency } from "@/common/utils/formatCurrency";
import { useI18n } from "@/common/hooks/useI18n";
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

type CreateUserFormValues = CreateUtilisateurDTO & {
  role: string;
  statut: string;
};

export default function AdminUsers(): ReactElement {
  const [searchQuery, setSearchQuery] = useState("");
  const [openCreateModal, setOpenCreateModal] = useState(false);
  const { t: tAdmin } = useI18n("admin");
  const { t: tRoles } = useI18n("roles");
  const { t: tStatus } = useI18n("status");
  const { data: usersData, isLoading, error } = useUsers();
  const { mutateAsync: createUser, isPending: isCreating } = useCreateUser();

  const users = (usersData ?? []).map((user: any) => ({
    id: user.uuid,
    name: [user.prenom, user.nom].filter(Boolean).join(" ").trim(),
    email: user.email,
    avatar: user.photoProfil || "",
    orders: 0,
    spent: 0,
    role: user.role === "ADMIN" ? "admin" : "user",
    status: user.statut === "ACTIF" ? "active" : "banned",
  }));

  const filteredUsers = users.filter(
    (user) =>
      user.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      user.email.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  const getStatusBadge = (status: string): ReactElement => {
    switch (status) {
      case "active":
        return (
          <Badge className="bg-green-100 text-green-800 hover:bg-green-100">
            {tStatus("active")}
          </Badge>
        );
      case "banned":
        return (
          <Badge className="bg-destructive/10 text-destructive hover:bg-destructive/10">
            {tStatus("banned")}
          </Badge>
        );
      default:
        return <Badge>{tStatus(status)}</Badge>;
    }
  };

  const getRoleBadge = (role: string): ReactElement => {
    switch (role) {
      case "admin":
        return (
          <Badge className="bg-primary/10 text-primary hover:bg-primary/10">
            {tRoles("admin")}
          </Badge>
        );
      default:
        return <Badge variant="outline">{tRoles("user")}</Badge>;
    }
  };

  const handleCreateUser = async (data: CreateUserFormValues) => {
    try {
      await createUser({
        nom: data.nom,
        prenom: data.prenom,
        email: data.email,
        motDePasse: data.motDePasse,
        statut: data.statut,
        role: data.role,
      });

      toast.success("Utilisateur créé", {
        description: `${data.prenom} ${data.nom} a été créé avec succès`,
      });
      setOpenCreateModal(false);
    } catch (err: any) {
      const errorMessage = err?.response?.data?.message || err?.message;
      toast.error("Erreur lors de la création", {
        description: errorMessage,
      });
    }
  };

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
        <CardHeader>
          <div className="flex items-center gap-4">
            <div className="relative flex-1 max-w-sm">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder={tAdmin("users.search")}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9"
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
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
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>{tAdmin("users.name")}</TableHead>
                <TableHead>{tAdmin("users.role")}</TableHead>
                <TableHead className="text-right">
                  {tAdmin("users.orders")}
                </TableHead>
                <TableHead className="text-right">
                  {tAdmin("users.spent")}
                </TableHead>
                <TableHead>{tAdmin("users.status")}</TableHead>
                <TableHead className="text-right">
                  {tAdmin("users.actions")}
                </TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {!isLoading &&
                !error &&
                filteredUsers.map((user) => (
                  <TableRow key={user.id}>
                    <TableCell>
                      <div className="flex items-center gap-3">
                        <Avatar className="h-8 w-8">
                          <AvatarImage src={user.avatar} />
                          <AvatarFallback>
                            {user.name
                              .split(" ")
                              .map((n) => n[0])
                              .join("")}
                          </AvatarFallback>
                        </Avatar>
                        <div>
                          <p className="font-medium">{user.name}</p>
                          <p className="text-sm text-muted-foreground">
                            {user.email}
                          </p>
                        </div>
                      </div>
                    </TableCell>
                    <TableCell>{getRoleBadge(user.role)}</TableCell>
                    <TableCell className="text-right">{user.orders}</TableCell>
                    <TableCell className="text-right">
                      {formatCurrency(user.spent)}
                    </TableCell>
                    <TableCell>{getStatusBadge(user.status)}</TableCell>
                    <TableCell className="text-right">
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="icon">
                            <MoreHorizontal className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem>
                            <Mail className="h-4 w-4 mr-2" />
                            {tAdmin("users.sendEmail")}
                          </DropdownMenuItem>
                          <DropdownMenuItem>
                            <Shield className="h-4 w-4 mr-2" />
                            {tAdmin("users.makeAdmin")}
                          </DropdownMenuItem>
                          <DropdownMenuItem className="text-destructive">
                            <Ban className="h-4 w-4 mr-2" />
                            {tAdmin("users.banUser")}
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>
                ))}
            </TableBody>
          </Table>
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
                items: [
                  { label: "Utilisateur", value: RoleUtilisateur.UTILISATEUR },
                  { label: "Vendeur", value: RoleUtilisateur.VENDEUR },
                  { label: "Admin", value: RoleUtilisateur.ADMIN },
                ],
              },
            ]}
          />
        </DialogContent>
      </Dialog>
    </div>
  );
}
