import { Dialog, DialogContent } from "@/common/components/ui/dialog";
import Form, { type IFieldGroup } from "@/common/containers/Form";
import { useForm } from "react-hook-form";
import * as z from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { DialogTitle } from "@radix-ui/react-dialog";
import { useEffect, useMemo, useState } from "react";
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from "@/common/components/ui/tabs";
import type { ColumnDef } from "@tanstack/react-table";
import { Badge } from "@/common/components/ui/badge";
import { Star, MapPin, Phone, Calendar } from "lucide-react";
import { DataTable } from "@/common/components";
import { formatCurrency } from "@/common/utils/formatCurrency";

// Define CreateDealInput type
type CreateDealInput = {
  title: string;
  shortSubtitle?: string;
  description: string;
  price: number;
  originalPrice: number;
  currency: string;
  partsTotal: number;
  minRequired: number;
  expiryDate?: string;
  location: string;
  categoryId: string;
  highlights?: string;
  whatsIncluded: string;
  images: string[];
  status: string;
  supplierName?: string;
  packagingMethod?: string;
};

// Define viewDetailDealFormSchema
const viewDetailDealFormSchema = z.object({
  title: z.string(),
  shortSubtitle: z.string().optional(),
  description: z.string(),
  price: z.number(),
  originalPrice: z.number(),
  currency: z.string(),
  partsTotal: z.number(),
  minRequired: z.number(),
  expiryDate: z.string().optional(),
  location: z.string(),
  categoryId: z.string(),
  highlights: z.string().optional(),
  whatsIncluded: z.string(),
  images: z.array(z.string()),
  status: z.string(),
  supplierName: z.string().optional(),
  packagingMethod: z.string().optional(),
});

// Mock data for participants and reviews
const participantsMock: ParticipantRow[] = [
  {
    id: "1",
    name: "Participant 1",
    phone: "123456789",
    email: "participant1@example.com",
    parts: 2,
    amount: 100000,
    status: "confirmé",
    shippingAddress: {
      street: "123 Rue Sainte-Catherine Ouest",
      city: "Montréal",
      province: "QC",
      postalCode: "H3B 1E3",
      country: "Canada",
    },
  },
  {
    id: "2",
    name: "Participant 2",
    phone: "987654321",
    email: "participant2@example.com",
    parts: 1,
    amount: 50000,
    status: "en attente",
    shippingAddress: {
      street: "456 King Street West",
      city: "Toronto",
      province: "ON",
      postalCode: "M5V 1L7",
      country: "Canada",
    },
  },
];

const reviewsMock: ReviewRow[] = [
  {
    id: "1",
    user: "User 1",
    rating: 4,
    comment: "Great deal!",
    date: "2023-10-01",
  },
  {
    id: "2",
    user: "User 2",
    rating: 5,
    comment: "Excellent!",
    date: "2023-10-02",
  },
];

const supplierMock = {
  name: "Supplier Name",
  rating: 4.5,
  city: "Supplier City",
  contact: "Supplier Contact",
  dealsCount: 10,
  joinedAt: "2022-01-01",
};

type ParticipantRow = {
  id: string;
  name: string;
  phone: string;
  email: string;
  parts: number;
  amount: number;
  status: "confirmé" | "en attente";
  shippingAddress: {
    street: string;
    city: string;
    province: string;
    postalCode: string;
    country: string;
  };
};

type ReviewRow = {
  id: string;
  user: string;
  rating: number;
  comment: string;
  date: string;
};

export function ViewDetailDealModal({
  open,
  onClose,
  deal,
}: {
  open: boolean;
  onClose: () => void;
  deal: any;
}) {
  const form = useForm<CreateDealInput>({
    resolver: zodResolver(viewDetailDealFormSchema),
  });

  const participantColumns = useMemo<ColumnDef<ParticipantRow>[]>(
    () => [
      {
        accessorKey: "name",
        header: "Nom du participant",
        cell: ({ row }) => (
          <div className="flex flex-col">
            <span className="font-medium text-foreground">
              {row.original.name}
            </span>
            <span className="text-xs text-muted-foreground">
              {row.original.phone}
            </span>
          </div>
        ),
      },
      {
        accessorKey: "email",
        header: "Email",
        cell: ({ row }) => (
          <span className="font-medium text-foreground">
            {row.original.email}
          </span>
        ),
      },
      {
        accessorKey: "shippingAddress",
        header: "Adresse de livraison",
        cell: ({ row }) => {
          const address = row.original.shippingAddress;

          if (!address) return <span className="text-muted-foreground">—</span>;

          return (
            <span className="font-semibold text-center block">
              {address.street}, {address.city}, {address.province}{" "}
              {address.postalCode}, {address.country}
            </span>
          );
        },
      },
      {
        accessorKey: "parts",
        header: "Nombre de parts",
        cell: ({ row }) => (
          <span className="font-semibold text-center">
            {row.original.parts}
          </span>
        ),
      },
      {
        accessorKey: "amount",
        header: "Montant",
        cell: ({ row }) => (
          <span className="font-semibold text-primary">
            {formatCurrency(row.original.amount)}
          </span>
        ),
      },
      {
        accessorKey: "status",
        header: "Statut",
        cell: ({ row }) => (
          <Badge
            colorScheme={
              row.original.status === "confirmé" ? "success" : "warning"
            }
            className={
              row.original.status === "confirmé"
                ? "bg-green-100 text-green-800"
                : "bg-yellow-100 text-yellow-800"
            }
          >
            {row.original.status === "confirmé"
              ? "✓ Confirmé"
              : "⏳ En attente"}
          </Badge>
        ),
      },
    ],
    []
  );

  const reviewColumns = useMemo<ColumnDef<ReviewRow>[]>(
    () => [
      {
        accessorKey: "user",
        header: "Auteur",
        cell: ({ row }) => (
          <span className="font-medium">{row.original.user}</span>
        ),
      },
      {
        accessorKey: "rating",
        header: "Note",
        cell: ({ row }) => (
          <div className="flex items-center gap-1">
            {Array.from({ length: 5 }).map((_, i) => (
              <Star
                key={i}
                className={`w-4 h-4 ${
                  i < row.original.rating
                    ? "fill-yellow-400 text-yellow-400"
                    : "text-muted-foreground"
                }`}
              />
            ))}
            <span className="ml-1 text-sm font-semibold">
              {row.original.rating}/5
            </span>
          </div>
        ),
      },
      {
        accessorKey: "comment",
        header: "Commentaire",
        cell: ({ row }) => (
          <p className="text-sm text-muted-foreground max-w-xs">
            {row.original.comment}
          </p>
        ),
      },
      {
        accessorKey: "date",
        header: "Date",
        cell: ({ row }) => (
          <span className="text-xs text-muted-foreground">
            {row.original.date}
          </span>
        ),
      },
    ],
    []
  );

  const [partsTotal, setPartsTotal] = useState<number>(0);

  useEffect(() => {
    if (deal) {
      form.reset(normalizeDealForModal(deal));
      setPartsTotal(Number(deal.total));
    }
  }, [deal, form]);

  function normalizeDealForModal(raw: any): CreateDealInput {
    return {
      title: raw.title ?? "",
      shortSubtitle: undefined,
      description: "",
      price: Number(raw.groupPrice) * Number(raw.unit),
      originalPrice: Number(raw.originalPrice),
      currency: "XAF",
      partsTotal: Number(raw.total),
      minRequired: 1,
      expiryDate: undefined,
      location: raw.city ?? "",
      categoryId: raw.category ?? "",
      highlights: raw.discount ? `Remise de ${raw.discount}%` : undefined,
      whatsIncluded: `• ${raw.unit} ${raw.currency} par part\n• Produit : ${raw.title}`,
      images: [],
      status: "published",
      supplierName: undefined,
      packagingMethod: undefined,
    };
  }

  const viewDetailDealFormGroups: IFieldGroup[] = [
    {
      title: "Informations générales",
      columns: 2,
      fields: [
        {
          type: "text",
          name: "title",
          label: "Titre du deal",
        },
        {
          type: "text",
          name: "shortSubtitle",
          label: "Sous-titre",
        },
        {
          type: "textarea",
          name: "description",
          label: "Description",
          colSpan: 2,
        },
        {
          type: "text",
          name: "location",
          label: "Lieu",
        },
        {
          type: "radio",
          name: "status",
          label: "Statut",
          items: [
            { label: "Publié", value: "published" },
            { label: "Brouillon", value: "draft" },
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
          label: "Prix de la part (USD)",
        },
        {
          type: "number",
          name: "originalPrice",
          label: "Prix initial (USD)",
        },
        {
          type: "select",
          name: "currency",
          label: "Devise",
          items: [{ label: "Dollar", value: "USD" }],
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
          label: "Parts totales",
        },
        {
          type: "number",
          name: "minRequired",
          label: "Parts minimum",
        },
        {
          type: "date",
          name: "expiryDate",
          label: "Date d’expiration",
        },
      ],
    },
  ];

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent size="xl" className="p-0 h-[90vh] overflow-hidden">
        <DialogTitle className="px-6 py-4 border-b font-semibold">
          Détails du deal
        </DialogTitle>

        <div className="h-[calc(90vh-72px)] overflow-y-auto">
          <Tabs defaultValue="details" className="w-full">
            <TabsList className="grid w-full grid-cols-4 rounded-none border-b bg-muted/30 px-0">
              <TabsTrigger
                value="details"
                className="rounded-none border-b-2 border-transparent data-[state=active]:border-primary data-[state=active]:bg-transparent"
              >
                <span className="text-sm">📄 Détails</span>
              </TabsTrigger>
              <TabsTrigger
                value="participants"
                className="rounded-none border-b-2 border-transparent data-[state=active]:border-primary data-[state=active]:bg-transparent"
              >
                <span className="text-sm">👥 Participants</span>
              </TabsTrigger>
              <TabsTrigger
                value="supplier"
                className="rounded-none border-b-2 border-transparent data-[state=active]:border-primary data-[state=active]:bg-transparent"
              >
                <span className="text-sm">🏪 Fournisseur</span>
              </TabsTrigger>
              <TabsTrigger
                value="reviews"
                className="rounded-none border-b-2 border-transparent data-[state=active]:border-primary data-[state=active]:bg-transparent"
              >
                <span className="text-sm">⭐ Avis</span>
              </TabsTrigger>
            </TabsList>

            <TabsContent
              value="details"
              className="px-6 py-4 h-[calc(90vh-72px)] overflow-y-auto"
            >
              <Form<CreateDealInput>
                form={form}
                groups={viewDetailDealFormGroups}
                readOnly
              />
            </TabsContent>

            <TabsContent value="participants" className="px-6 py-4">
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-lg font-semibold">
                      Participants au deal
                    </h3>
                    <p className="text-sm text-muted-foreground">
                      {participantsMock.length} participant
                      {participantsMock.length > 1 ? "s" : ""} confirmé
                      {participantsMock.length > 1 ? "s" : ""}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-2xl font-bold text-primary">
                      {participantsMock.reduce((sum, p) => sum + p.parts, 0)}/
                      {partsTotal}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      parts utilisées
                    </p>
                  </div>
                </div>
                <DataTable<ParticipantRow, unknown>
                  columns={participantColumns}
                  data={participantsMock}
                  searchKey="name"
                  searchPlaceholder="Rechercher un participant..."
                  enableSelection={false}
                  enableRowNumber={true}
                  enableExport={true}
                  enableSorting={true}
                  pageSizeOptions={[5, 10, 20]}
                />
              </div>
            </TabsContent>

            <TabsContent value="supplier" className="px-6 py-4">
              <div className="space-y-6">
                <div className="grid gap-6">
                  <div className="space-y-2">
                    <h3 className="text-2xl font-bold">{supplierMock.name}</h3>
                    <div className="flex items-center gap-2">
                      <div className="flex items-center gap-1">
                        {Array.from({ length: 5 }).map((_, i) => (
                          <Star
                            key={i}
                            className={`w-4 h-4 ${
                              i < Math.floor(supplierMock.rating)
                                ? "fill-yellow-400 text-yellow-400"
                                : "text-muted-foreground"
                            }`}
                          />
                        ))}
                      </div>
                      <span className="font-semibold">
                        {supplierMock.rating} / 5
                      </span>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div className="rounded-lg border border-border/50 bg-muted/30 p-4">
                      <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">
                        Localisation
                      </p>
                      <div className="mt-2 flex items-center gap-2">
                        <MapPin className="h-5 w-5 text-primary" />
                        <span className="font-semibold">
                          {supplierMock.city}
                        </span>
                      </div>
                    </div>

                    <div className="rounded-lg border border-border/50 bg-muted/30 p-4">
                      <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">
                        Contact
                      </p>
                      <div className="mt-2 flex items-center gap-2">
                        <Phone className="h-5 w-5 text-primary" />
                        <span className="font-semibold">
                          {supplierMock.contact}
                        </span>
                      </div>
                    </div>

                    <div className="rounded-lg border border-border/50 bg-muted/30 p-4">
                      <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">
                        Deals publiés
                      </p>
                      <p className="mt-2 text-2xl font-bold text-primary">
                        {supplierMock.dealsCount}
                      </p>
                    </div>

                    <div className="rounded-lg border border-border/50 bg-muted/30 p-4">
                      <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">
                        Membre depuis
                      </p>
                      <div className="mt-2 flex items-center gap-2">
                        <Calendar className="h-5 w-5 text-primary" />
                        <span className="font-semibold">
                          {new Date(supplierMock.joinedAt).toLocaleDateString(
                            "fr-FR"
                          )}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="reviews" className="px-6 py-4">
              <div className="space-y-4">
                <div>
                  <h3 className="text-lg font-semibold">Avis clients</h3>
                  <p className="text-sm text-muted-foreground">
                    {reviewsMock.length} avis reçus
                  </p>
                </div>
                <DataTable<ReviewRow, unknown>
                  columns={reviewColumns}
                  data={reviewsMock}
                  searchKey="user"
                  searchPlaceholder="Rechercher un avis..."
                  enableSelection={false}
                  enableRowNumber={false}
                  enableExport={true}
                  enableSorting={true}
                  pageSizeOptions={[5, 10, 20]}
                />
              </div>
            </TabsContent>
          </Tabs>
        </div>
      </DialogContent>
    </Dialog>
  );
}
