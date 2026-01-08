import {
  Dialog,
  DialogContent,
  DialogHeader,
} from "@/common/components/ui/dialog";
import Form, { type IFieldGroup } from "@/common/containers/Form";
import { useForm } from "react-hook-form";
import * as z from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { categories } from "@/common/constants/data";
import { DialogTitle } from "@radix-ui/react-dialog";

// ==============================
// Types
// ==============================
export type CreateDealInput = {
  title: string;
  shortSubtitle?: string;
  description: string;

  price: number;
  originalPrice?: number;
  currency: "XAF";

  partsTotal: number;
  minRequired: number;
  expiryDate?: Date;

  location: string;
  categoryId: string;

  highlights?: string;
  whatsIncluded?: string;

  images: File[];
  status: "draft" | "published";

  supplierName?: string;
  packagingMethod?: string;
};

// ==============================
// Schema Zod
// ==============================
export const createDealFormSchema = z.object({
  title: z.string().min(3, "Titre trop court").max(100),
  shortSubtitle: z.string().optional(),
  description: z.string().min(10, "Description trop courte"),

  price: z.number().min(0),
  originalPrice: z.number().optional(),
  currency: z.enum(["XAF"]),

  partsTotal: z.number().min(1),
  minRequired: z.number().min(1),
  expiryDate: z.date().optional(),

  location: z.string().min(3),
  categoryId: z.string(),

  highlights: z.string().optional(),
  whatsIncluded: z.string().optional(),

  images: z.any(), // upload géré côté UI
  status: z.enum(["draft", "published"]),

  supplierName: z.string().optional(),
  packagingMethod: z.string().optional(),
});

// ==============================
// Component
// ==============================
export function CreateDealModal({
  open,
  onClose,
}: {
  open: boolean;
  onClose: () => void;
}) {
  const form = useForm<CreateDealInput>({
    defaultValues: {
      currency: "XAF",
      status: "draft",
      images: [],
    },
    resolver: zodResolver(createDealFormSchema),
  });

  const createDealFormGroups: IFieldGroup[] = [
    {
      title: "Informations générales",
      description: "Informations visibles par les clients",
      columns: 2,
      fields: [
        {
          type: "text",
          name: "title",
          label: "Titre du deal",
          placeholder: "Ex : Dîner romantique pour 2 personnes",
        },
        {
          type: "text",
          name: "shortSubtitle",
          label: "Sous-titre",
          placeholder: "Restaurant chic au centre-ville",
        },
        {
          type: "textarea",
          name: "description",
          label: "Description complète",
          colSpan: 2,
          placeholder: "Décrivez l’offre en détail...",
        },
        {
          type: "select",
          name: "categoryId",
          label: "Catégorie",
          items: categories.map((c) => ({
            label: c.name,
            value: c.id.toString(),
          })),
        },
        {
          type: "radio",
          name: "status",
          label: "Statut du deal",
          items: [
            { label: "Brouillon", value: "draft" },
            { label: "Publié", value: "published" },
          ],
        },
      ],
    },

    {
      title: "Tarification",
      columns: 3,
      fields: [
        {
          type: "number",
          name: "price",
          label: "Prix promo (FCFA)",
        },
        {
          type: "number",
          name: "originalPrice",
          label: "Prix initial",
        },
        {
          type: "select",
          name: "currency",
          label: "Devise",
          items: [{ label: "FCFA (XAF)", value: "XAF" }],
        },
      ],
    },

    {
      title: "Disponibilité",
      columns: 3,
      fields: [
        {
          type: "number",
          name: "partsTotal",
          label: "Nombre total de parts",
        },
        {
          type: "number",
          name: "minRequired",
          label: "Parts minimum requises",
        },
        {
          type: "date",
          name: "expiryDate",
          label: "Date d’expiration",
        },
      ],
    },

    {
      title: "Localisation",
      columns: 2,
      fields: [
        {
          type: "text",
          name: "location",
          label: "Lieu",
          placeholder: "Douala – Bonapriso",
        },
      ],
    },

    {
      title: "Contenu de l’offre",
      columns: 2,
      fields: [
        {
          type: "textarea",
          name: "highlights",
          label: "Points forts",
          placeholder: "• Cadre romantique\n• Menu gastronomique",
        },
        {
          type: "textarea",
          name: "whatsIncluded",
          label: "Inclus dans l’offre",
          placeholder: "• Entrée\n• Plat principal\n• Dessert",
        },
      ],
    },

    {
      title: "Fournisseur & logistique",
      columns: 2,
      fields: [
        {
          type: "text",
          name: "supplierName",
          label: "Nom du fournisseur",
        },
        {
          type: "text",
          name: "packagingMethod",
          label: "Méthode de packaging",
          placeholder: "Sur place / À emporter",
        },
      ],
    },
  ];

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent size="xl" className="p-0 h-[90vh] overflow-hidden">
        <DialogTitle className="px-6 py-4 border-b font-semibold">
          Ajouter un nouveau deal
        </DialogTitle>

        <div className="h-[calc(90vh-72px)] overflow-y-auto px-6 py-4">
          <Form<CreateDealInput>
            form={form}
            groups={createDealFormGroups}
            submitLabel="Créer le deal"
            onSubmit={async ({ data }) => {
              console.log(data);
              onClose();
            }}
          />
        </div>
      </DialogContent>
    </Dialog>
  );
}
