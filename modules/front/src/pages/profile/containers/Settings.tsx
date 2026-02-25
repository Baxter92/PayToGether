import { useState } from "react";
import { useI18n } from "@hooks/useI18n";
import Form, { type IFieldConfig } from "@/common/containers/Form";
import { Heading } from "@/common/containers/Heading";
import { useAuth } from "@/common/context/AuthContext";
import {
  useResetUserPassword,
  useUpdateUser,
} from "@/common/api/hooks/useUtilisateurs";
import { Button } from "@/common/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/common/components/ui/dialog";
import { toast } from "sonner";
import * as z from "zod";
import type { UpdateUtilisateurDTO } from "@/common/api";

type PasswordFormValues = {
  nouveauMotDePasse: string;
};

export default function Settings() {
  const { t } = useI18n();
  const { user, logout } = useAuth();
  const [openChangePasswordModal, setOpenChangePasswordModal] = useState(false);
  const { mutateAsync: resetUserPassword, isPending: isResettingPassword } =
    useResetUserPassword();
  const { mutateAsync: updateUser, isPending: isUpdatingUser } =
    useUpdateUser();

  const fields: IFieldConfig[] = [
    {
      name: "email",
      label: t("profile:emailLabel"),
      type: "email",
      placeholder: "Email",
      readOnly: true,
    },
    {
      name: "prenom",
      label: t("profile:firstNameLabel"),
      type: "text",
      placeholder: t("profile:firstNameLabel"),
    },
    {
      name: "nom",
      label: t("profile:lastNameLabel"),
      type: "text",
      placeholder: t("profile:lastNameLabel"),
    },
    // {
    //   name: "phone",
    //   label: t("profile:phoneLabel"),
    //   type: "text",
    //   placeholder: t("profile:phoneLabel"),
    // },
  ];

  const handleUpdateUser = async (data: UpdateUtilisateurDTO) => {
    if (!user?.id) {
      logout();
      return;
    }
    delete data?.email;
    try {
      await updateUser({
        id: user.id,
        data,
      });
      toast.success("Profil mis à jour");
    } catch (err: any) {
      toast.error("Erreur lors de la mise à jour du profil", {
        description: err?.message ?? "Une erreur est survenue",
      });
    }
  };

  const handleChangePassword = async (data: PasswordFormValues) => {
    if (!user?.id) {
      toast.error("Utilisateur introuvable");
      return;
    }

    try {
      await resetUserPassword({
        utilisateurUuid: user.id,
        data: { nouveauMotDePasse: data.nouveauMotDePasse },
      });
      toast.success("Mot de passe mis à jour");
      setOpenChangePasswordModal(false);
    } catch (err: any) {
      toast.error("Erreur lors du changement de mot de passe", {
        description: err?.message ?? "Une erreur est survenue",
      });
    }
  };

  return (
    <section>
      <Heading
        level={2}
        title={t("profile:settingsTitle")}
        description={t("profile:settingsDescription")}
        underline
      />
      <Form
        fields={fields}
        schema={z.object({
          email: z.email(),
          prenom: z.string(),
          nom: z.string(),
        })}
        defaultValues={{
          email: user?.email,
          prenom: user?.prenom,
          nom: user?.nom,
        }}
        onSubmit={async ({ data }) => {
          await handleUpdateUser(data as UpdateUtilisateurDTO);
        }}
        isLoading={isUpdatingUser}
      />

      <div className="mt-6">
        <Button onClick={() => setOpenChangePasswordModal(true)}>
          {t("profile:changePassword")}
        </Button>
      </div>

      <Dialog
        open={openChangePasswordModal}
        onOpenChange={setOpenChangePasswordModal}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("profile:changePassword")}</DialogTitle>
            <DialogDescription>
              {t("profile:changePasswordDescription")}
            </DialogDescription>
          </DialogHeader>

          <Form<PasswordFormValues>
            fields={[
              {
                name: "nouveauMotDePasse",
                label: t("profile:changePassword"),
                type: "password",
                placeholder: t("auth:password"),
              },
            ]}
            schema={z.object({
              nouveauMotDePasse: z.string().min(8),
            })}
            defaultValues={{ nouveauMotDePasse: "" }}
            onSubmit={async ({ data }) => {
              await handleChangePassword(data);
            }}
            submitLabel={t("profile:changePassword")}
            isLoading={isResettingPassword}
            showResetButton={false}
          />
        </DialogContent>
      </Dialog>
    </section>
  );
}
