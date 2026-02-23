import { useState } from "react";
import { useI18n } from "@hooks/useI18n";
import Form, { type IFieldConfig } from "@/common/containers/Form";
import { Heading } from "@/common/containers/Heading";
import { useAuth } from "@/common/context/AuthContext";
import { useResetUserPassword } from "@/common/api/hooks/useUtilisateurs";
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

type PasswordFormValues = {
  nouveauMotDePasse: string;
};

export default function Settings() {
  const { t } = useI18n("profile");
  const { user } = useAuth();
  const [openChangePasswordModal, setOpenChangePasswordModal] = useState(false);
  const { mutateAsync: resetUserPassword, isPending: isResettingPassword } =
    useResetUserPassword();

  const fields: IFieldConfig[] = [
    {
      name: "email",
      label: t("emailLabel"),
      type: "email",
      placeholder: "Email",
      readOnly: true,
    },
    {
      name: "firstName",
      label: t("firstNameLabel"),
      type: "text",
      placeholder: t("firstNameLabel"),
    },
    {
      name: "lastName",
      label: t("lastNameLabel"),
      type: "text",
      placeholder: t("lastNameLabel"),
    },
    {
      name: "phone",
      label: t("phoneLabel"),
      type: "text",
      placeholder: t("phoneLabel"),
    },
  ];

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
        title={t("settingsTitle")}
        description={t("settingsDescription")}
        underline
      />
      <Form
        fields={fields}
        schema={z.object({
          email: z.email(),
          firstName: z.string(),
          lastName: z.string(),
          phone: z.string(),
        })}
        defaultValues={{
          email: user?.email,
          firstName: user?.name,
          lastName: user?.name,
        }}
        onSubmit={(data) => console.log(data)}
      />

      <div className="mt-6">
        <Button onClick={() => setOpenChangePasswordModal(true)}>
          {t("changePassword")}
        </Button>
      </div>

      <Dialog
        open={openChangePasswordModal}
        onOpenChange={setOpenChangePasswordModal}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("changePassword")}</DialogTitle>
            <DialogDescription>
              Définissez un nouveau mot de passe.
            </DialogDescription>
          </DialogHeader>

          <Form<PasswordFormValues>
            fields={[
              {
                name: "nouveauMotDePasse",
                label: t("changePassword"),
                type: "password",
                placeholder: t("auth.password"),
              },
            ]}
            schema={z.object({
              nouveauMotDePasse: z.string().min(8),
            })}
            defaultValues={{ nouveauMotDePasse: "" }}
            onSubmit={async ({ data }) => {
              await handleChangePassword(data);
            }}
            submitLabel={t("changePassword")}
            isLoading={isResettingPassword}
            showResetButton={false}
          />
        </DialogContent>
      </Dialog>
    </section>
  );
}
