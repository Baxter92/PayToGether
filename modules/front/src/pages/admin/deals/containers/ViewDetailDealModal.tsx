import { useI18n } from "@/common/hooks/useI18n";
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
import {
  Star,
  MapPin,
  Phone,
  Calendar,
  Users,
  MessageSquare,
  Store,
  FileText,
  Sparkles,
  TrendingUp,
  Package,
  ImageIcon,
  Loader2,
  Expand,
} from "lucide-react";
import { DataTable } from "@/common/components";
import { formatCurrency } from "@/common/utils/formatCurrency";
import {
  useCommentairesByDeal,
  useUsers,
  useGetDealImageUrl,
  useDealParticipants,
  StatutPaiement,
  type ParticipantDto,
} from "@/common/api";
import { cn } from "@/common/utils";
import { ImageLightbox } from "@/common/components/ImageLightbox";
import { Button } from "@/common/components/ui/button";

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

const supplierMock = {
  name: "Supplier Name",
  rating: 4.5,
  city: "Supplier City",
  contact: "Supplier Contact",
  dealsCount: 10,
  joinedAt: "2022-01-01",
};

type ReviewRow = {
  id: string;
  user: string;
  rating: number;
  comment: string;
  date: string;
};

/* ==============================
   ReadOnlyImageGallery Component
============================== */

function ReadOnlyImageThumbnail({
  image,
  index,
  dealUuid,
  onImageClick,
}: {
  image: any;
  index: number;
  dealUuid?: string;
  onImageClick: () => void;
}) {
  const isPrincipal = !!image.isPrincipal;
  const imageUuid = image.imageUuid || image.uuid;

  const { data: imageUrlData } = useGetDealImageUrl(
    dealUuid ?? "",
    imageUuid ?? "",
  );

  const src = imageUrlData?.url;

  return (
    <div
      className={cn(
        "relative aspect-[4/3] rounded-xl overflow-hidden bg-white dark:bg-gray-900 group",
        "border-2 transition-all duration-300 shadow-sm cursor-pointer",
        isPrincipal
          ? "border-blue-500 ring-2 ring-blue-200/50 dark:ring-blue-800/50 shadow-blue-100 dark:shadow-blue-900/30"
          : "border-gray-200 dark:border-gray-700",
      )}
      onClick={onImageClick}
    >
      {src ? (
        <div className="w-full h-full bg-gray-50 dark:bg-gray-800">
          <img
            src={src}
            alt={`Image ${index + 1}`}
            className="w-full h-full object-cover"
          />
        </div>
      ) : (
        <div className="w-full h-full flex items-center justify-center bg-gray-100 dark:bg-gray-800">
          <Loader2 className="w-6 h-6 text-gray-300 dark:text-gray-600 animate-spin" />
        </div>
      )}

      {/* Badge principal */}
      {isPrincipal && (
        <div className="absolute top-3 left-3">
          <div className="flex items-center gap-1.5 bg-gradient-to-r from-blue-600 to-indigo-600 text-white text-xs font-bold px-3 py-1.5 rounded-full shadow-lg">
            <Star className="w-3.5 h-3.5 fill-current" />
            PRINCIPALE
          </div>
        </div>
      )}

      {/* Numéro */}
      <div className="absolute top-3 right-3">
        <div className="w-8 h-8 rounded-full bg-white dark:bg-gray-700 flex items-center justify-center text-sm font-bold text-gray-700 dark:text-gray-200 shadow-md">
          {index + 1}
        </div>
      </div>

      {/* Bouton Full Screen en overlay */}
      {src && (
        <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
          <Button
            variant="secondary"
            size="icon"
            className="shadow-lg"
            onClick={(e) => {
              e.stopPropagation();
              onImageClick();
            }}
            title="Voir en plein écran"
          >
            <Expand className="w-5 h-5" />
          </Button>
        </div>
      )}
    </div>
  );
}

/* ==============================
   ReadOnlyImageThumbnailWrapper Component
============================== */

function ReadOnlyImageThumbnailWrapper({
  image,
  index,
  dealUuid,
  onImageClick,
}: {
  image: any;
  index: number;
  dealUuid?: string;
  onImageClick: () => void;
}) {
  return (
    <ReadOnlyImageThumbnail
      image={image}
      index={index}
      dealUuid={dealUuid}
      onImageClick={onImageClick}
    />
  );
}

/* ==============================
   ReadOnlyImageLightbox Component
============================== */

function ReadOnlyImageLightbox({
  images,
  dealUuid,
  currentIndex,
  isOpen,
  onClose,
}: {
  images: any[];
  dealUuid?: string;
  currentIndex: number;
  isOpen: boolean;
  onClose: () => void;
}) {
  // Charger toutes les URLs des images
  const imageUrls = images
    .map((img) => {
      const imageUuid = img.imageUuid || img.uuid;
      // eslint-disable-next-line react-hooks/rules-of-hooks
      const { data: imageUrlData } = useGetDealImageUrl(
        dealUuid ?? "",
        imageUuid ?? "",
      );
      return imageUrlData?.url || "";
    })
    .filter(Boolean);

  return (
    <ImageLightbox
      images={imageUrls}
      currentIndex={currentIndex}
      isOpen={isOpen}
      onClose={onClose}
    />
  );
}

/* ==============================
   ReadOnlyImageGallery Component
============================== */

function ReadOnlyImageGallery({
  images,
  dealUuid,
}: {
  images: any[];
  dealUuid?: string;
}) {
  const [isLightboxOpen, setIsLightboxOpen] = useState(false);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const { t } = useI18n("deals");

  const handleImageClick = (index: number): void => {
    setCurrentImageIndex(index);
    setIsLightboxOpen(true);
  };

  if (!images || images.length === 0) {
    return (
      <div className="border-2 border-dashed border-gray-200 dark:border-gray-700 rounded-2xl py-16 bg-gradient-to-br from-gray-50 to-blue-50/30 dark:from-gray-900 dark:to-blue-950/30">
        <div className="flex flex-col items-center text-center">
          <div className="w-20 h-20 rounded-2xl bg-white dark:bg-gray-800 shadow-md flex items-center justify-center mb-4 border-2 border-gray-100 dark:border-gray-700">
            <ImageIcon className="w-10 h-10 text-gray-300 dark:text-gray-600" />
          </div>
          <p className="text-base text-gray-900 dark:text-gray-100 font-bold mb-1">
            {t("deals.noImageAvailable")}
          </p>
          <p className="text-sm text-gray-500 dark:text-gray-400">
            {t("deals.noImageAvailableDescription")}
          </p>
        </div>
      </div>
    );
  }

  return (
    <>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between p-5 bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-950/30 dark:to-indigo-950/30 rounded-xl border-2 border-blue-100/50 dark:border-blue-900/50 shadow-sm">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-xl bg-white dark:bg-gray-800 shadow-sm flex items-center justify-center border border-blue-100 dark:border-blue-900">
              <ImageIcon className="w-5 h-5 text-blue-600 dark:text-blue-400" />
            </div>
            <div>
              <p className="text-sm font-bold text-gray-900 dark:text-gray-100">
                {t("deals.imageGallery")}
              </p>
              <p className="text-xs text-gray-600 dark:text-gray-400">
                {t("deals.enlargeImage")}
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2 px-4 py-2 bg-white dark:bg-gray-800 rounded-full shadow-sm border-2 border-blue-100 dark:border-blue-900">
            <span className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
              {images.length}
            </span>
            <span className="text-sm text-gray-500 dark:text-gray-400 font-medium">
              {t("deals.images", { count: images.length })}
            </span>
          </div>
        </div>

        {/* Images Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {images.map((img, idx) => (
            <ReadOnlyImageThumbnailWrapper
              key={img.imageUuid || img.uuid || idx}
              image={img}
              index={idx}
              dealUuid={dealUuid}
              onImageClick={() => handleImageClick(idx)}
            />
          ))}
        </div>
      </div>

      {/* Lightbox */}
      {isLightboxOpen && (
        <ReadOnlyImageLightbox
          images={images}
          dealUuid={dealUuid}
          currentIndex={currentImageIndex}
          isOpen={isLightboxOpen}
          onClose={() => setIsLightboxOpen(false)}
        />
      )}
    </>
  );
}

/* ==============================
   ViewDetailDealModal Component
============================== */

export function ViewDetailDealModal({
  open,
  onClose,
  deal,
}: {
  open: boolean;
  onClose: () => void;
  deal: any;
}) {
  const { t } = useI18n("admin");
  const dealUuid = deal?.id ?? deal?.raw?.uuid ?? "";
  const { data: commentaires = [] } = useCommentairesByDeal(dealUuid);
  const { data: users = [] } = useUsers();
  const { data: participants = [] } = useDealParticipants(dealUuid);

  const form = useForm<CreateDealInput>({
    resolver: zodResolver(viewDetailDealFormSchema),
  });

  const participantColumns = useMemo<ColumnDef<ParticipantDto>[]>(
    () => [
      {
        accessorKey: "name",
        header: t("deals.participantName"),
        cell: ({ row }) => (
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center shadow-md flex-shrink-0">
              <span className="text-white font-bold text-sm">
                {row.original.utilisateurNom.charAt(0).toUpperCase()}
              </span>
            </div>
            <div className="flex flex-col">
              <span className="font-semibold text-gray-900 dark:text-gray-100">
                {row.original.utilisateurNom}
              </span>
              <span className="text-xs text-gray-500 dark:text-gray-400 flex items-center gap-1">
                <Phone className="w-3 h-3" />
                {row.original.adresseNumeroPhone}
              </span>
            </div>
          </div>
        ),
      },
      {
        accessorKey: "email",
        header: t("deals.email"),
        cell: ({ row }) => (
          <span className="text-sm text-gray-700 dark:text-gray-300 font-medium">
            {row.original.utilisateurEmail}
          </span>
        ),
      },
      {
        accessorKey: "shippingAddress",
        header: t("deals.shippingAddress"),
        cell: ({ row }) => {
          const address = {
            rue: row.original.adresseRue,
            codePostal: row.original.adresseCodePostal,
            ville: row.original.adresseVille,
            pays: row.original.adressePays,
            province: row.original.adresseProvince,
            numeroPhone: row.original.adresseNumeroPhone,
          };

          if (!address)
            return (
              <span className="text-gray-400 dark:text-gray-500 text-sm">
                —
              </span>
            );

          return (
            <div className="flex items-start gap-2">
              <MapPin className="w-4 h-4 text-blue-600 dark:text-blue-400 mt-0.5 flex-shrink-0" />
              <div className="text-sm text-gray-700 dark:text-gray-300">
                <div className="font-medium">{address.rue}</div>
                <div className="text-gray-500 dark:text-gray-400 text-xs">
                  {address.ville}, {address.province} {address.codePostal}
                </div>
              </div>
            </div>
          );
        },
      },
      {
        accessorKey: "parts",
        header: t("deals.partsCount"),
        cell: ({ row }) => (
          <div className="flex items-center justify-center">
            <Badge className="bg-blue-100 text-blue-700 hover:bg-blue-100 font-bold px-3 py-1">
              {row.original.nombreDePart} part
              {row.original.nombreDePart > 1 ? "s" : ""}
            </Badge>
          </div>
        ),
      },
      {
        accessorKey: "amount",
        header: t("deals.amount"),
        cell: ({ row }) => (
          <span className="font-bold text-green-600 text-base">
            {formatCurrency(row.original.montantTotal)}
          </span>
        ),
      },
      {
        accessorKey: "status",
        header: t("deals.status"),
        cell: ({ row }) => (
          <Badge
            className={
              row.original.statutPaiement === StatutPaiement.CONFIRME
                ? "bg-green-100 text-green-800 hover:bg-green-100 font-semibold"
                : "bg-yellow-100 text-yellow-800 hover:bg-yellow-100 font-semibold"
            }
          >
            {row.original.statutPaiement === StatutPaiement.CONFIRME ? (
              <span className="flex items-center gap-1">
                <span className="w-2 h-2 rounded-full bg-green-500" />
                {t("deals.statusConfirmed")}
              </span>
            ) : (
              <span className="flex items-center gap-1">
                <span className="w-2 h-2 rounded-full bg-yellow-500 animate-pulse" />
                {t("deals.statusPending")}
              </span>
            )}
          </Badge>
        ),
      },
    ],
    [],
  );

  const reviewColumns = useMemo<ColumnDef<ReviewRow>[]>(
    () => [
      {
        accessorKey: "user",
        header: t("deals.author"),
        cell: ({ row }) => (
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-gradient-to-br from-yellow-400 to-orange-500 flex items-center justify-center shadow-md flex-shrink-0">
              <span className="text-white font-bold text-sm">
                {row.original.user.charAt(0).toUpperCase()}
              </span>
            </div>
            <span className="font-semibold text-gray-900 dark:text-gray-100">
              {row.original.user}
            </span>
          </div>
        ),
      },
      {
        accessorKey: "rating",
        header: t("deals.rating"),
        cell: ({ row }) => (
          <div className="flex flex-col gap-1">
            <div className="flex items-center gap-1">
              {Array.from({ length: 5 }).map((_, i) => (
                <Star
                  key={i}
                  className={`w-4 h-4 ${
                    i < row.original.rating
                      ? "fill-yellow-400 text-yellow-400"
                      : "text-gray-300 dark:text-gray-600"
                  }`}
                />
              ))}
            </div>
            <span className="text-xs text-gray-600 dark:text-gray-400 font-medium">
              {row.original.rating}/5 étoiles
            </span>
          </div>
        ),
      },
      {
        accessorKey: "comment",
        header: t("deals.comment"),
        cell: ({ row }) => (
          <div className="max-w-md">
            <p className="text-sm text-gray-700 dark:text-gray-300 line-clamp-2 leading-relaxed">
              {row.original.comment}
            </p>
          </div>
        ),
      },
      {
        accessorKey: "date",
        header: t("deals.date"),
        cell: ({ row }) => (
          <div className="flex items-center gap-2 text-gray-600 dark:text-gray-400">
            <Calendar className="w-4 h-4" />
            <span className="text-xs font-medium">{row.original.date}</span>
          </div>
        ),
      },
    ],
    [],
  );

  const reviews = useMemo<ReviewRow[]>(() => {
    const usersByUuid = new Map(
      users.map((user) => [
        user.uuid,
        `${user.prenom ?? ""} ${user.nom ?? ""}`.trim() || user.email,
      ]),
    );

    return commentaires
      .filter((commentaire) => !commentaire.commentaireParentUuid)
      .sort(
        (a, b) =>
          new Date(b.dateCreation ?? 0).getTime() -
          new Date(a.dateCreation ?? 0).getTime(),
      )
      .map((commentaire) => ({
        id: commentaire.uuid ?? "",
        user:
          usersByUuid.get(commentaire.utilisateurUuid) ??
          `Utilisateur ${commentaire.utilisateurUuid.slice(0, 8)}`,
        rating: Number(commentaire.note) || 0,
        comment: commentaire.contenu,
        date: commentaire.dateCreation
          ? new Date(commentaire.dateCreation).toLocaleDateString("fr-FR")
          : "",
      }));
  }, [commentaires, users]);

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
      currency: "USD",
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
      title: t("deals.generalInformation"),
      description: t("deals.generalInformationDescription"),
      columns: 2,
      className:
        "bg-white dark:bg-gray-900 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700 p-6",
      fields: [
        {
          type: "text",
          name: "title",
          label: t("deals.dealTitle"),
        },
        {
          type: "text",
          name: "shortSubtitle",
          label: t("deals.subtitle"),
        },
        {
          type: "textarea",
          name: "description",
          label: t("deals.descriptionField"),
          colSpan: 2,
        },
        {
          type: "text",
          name: "location",
          label: t("deals.location"),
        },
        {
          type: "radio",
          name: "status",
          label: t("deals.status"),
          items: [
            { label: t("deals.published"), value: "published" },
            { label: t("deals.draft"), value: "draft" },
          ],
        },
      ],
    },
    {
      title: t("deals.pricing"),
      description: t("deals.pricingDescription"),
      columns: 3,
      className:
        "bg-white dark:bg-gray-900 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700 p-6",
      fields: [
        {
          type: "number",
          name: "price",
          label: t("deals.partPrice"),
        },
        {
          type: "number",
          name: "originalPrice",
          label: t("deals.initialPrice"),
        },
        {
          type: "select",
          name: "currency",
          label: t("deals.currency"),
          items: [{ label: t("deals.dollar"), value: "CAD" }],
        },
      ],
    },
    {
      title: t("deals.availability"),
      description: t("deals.availabilityDescription"),
      columns: 3,
      className:
        "bg-white dark:bg-gray-900 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700 p-6",
      fields: [
        {
          type: "number",
          name: "partsTotal",
          label: t("deals.totalParts"),
        },
        {
          type: "number",
          name: "minRequired",
          label: t("deals.minParts"),
        },
        {
          type: "date",
          name: "expiryDate",
          label: t("deals.expirationDate"),
        },
      ],
    },
    {
      title: t("deals.imageGallery"),
      description: t("deals.imageGalleryDescription"),
      className:
        "bg-white dark:bg-gray-900 rounded-xl shadow-sm border border-gray-100 dark:border-gray-700 p-6",
      fields: [
        {
          type: "file" as const,
          name: "images",
          label: t("deals.images"),
          render: () => (
            <ReadOnlyImageGallery
              images={deal?.raw?.listeImages || []}
              dealUuid={dealUuid}
            />
          ),
        },
      ],
    },
  ];

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent
        size="xl"
        className="p-0 h-[90vh] overflow-hidden bg-gradient-to-br from-gray-50 via-white to-blue-50/40 dark:from-gray-950 dark:via-gray-900 dark:to-blue-950/40"
      >
        {/* Header moderne */}
        <DialogTitle className="px-6 py-5 border-b border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 shadow-sm">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-blue-600 to-indigo-600 flex items-center justify-center shadow-lg">
              <Sparkles className="w-6 h-6 text-white" />
            </div>
            <div className="flex-1">
              <h2 className="text-xl font-bold text-gray-900 dark:text-gray-100">
                {t("deals.details")}
              </h2>
              <p className="text-sm text-gray-600 dark:text-gray-400 font-normal mt-0.5">
                {t("deals.detailsDescription")}
              </p>
            </div>
          </div>
        </DialogTitle>

        <div className="h-[calc(90vh-88px)] overflow-hidden">
          <Tabs defaultValue="details" className="w-full h-full flex flex-col">
            {/* Tabs avec design moderne */}
            <TabsList className="grid w-full grid-cols-4 rounded-none border-b border-gray-200 dark:border-gray-700 bg-gradient-to-r from-gray-50 to-blue-50/30 dark:from-gray-900 dark:to-blue-950/30 px-6 h-14">
              <TabsTrigger
                value="details"
                className="relative rounded-lg data-[state=active]:bg-white dark:data-[state=active]:bg-gray-800 data-[state=active]:shadow-sm transition-all duration-300"
              >
                <div className="flex items-center gap-2 px-2">
                  <FileText className="w-4 h-4" />
                  <span className="text-sm font-medium">
                    {t("deals.details")}
                  </span>
                </div>
              </TabsTrigger>
              <TabsTrigger
                value="participants"
                className="relative rounded-lg data-[state=active]:bg-white dark:data-[state=active]:bg-gray-800 data-[state=active]:shadow-sm transition-all duration-300"
              >
                <div className="flex items-center gap-2 px-2">
                  <Users className="w-4 h-4" />
                  <span className="text-sm font-medium">
                    {t("deals.participants")}
                  </span>
                  <Badge className="ml-1 bg-blue-600 text-white text-xs px-2 py-0.5">
                    {participants.length}
                  </Badge>
                </div>
              </TabsTrigger>
              {/* <TabsTrigger
                value="supplier"
                className="relative rounded-lg data-[state=active]:bg-white dark:data-[state=active]:bg-gray-800 data-[state=active]:shadow-sm transition-all duration-300"
              >
                <div className="flex items-center gap-2 px-2">
                  <Store className="w-4 h-4" />
                  <span className="text-sm font-medium">
                    {t("deals.supplier")}
                  </span>
                </div>
              </TabsTrigger> */}
              <TabsTrigger
                value="reviews"
                className="relative rounded-lg data-[state=active]:bg-white dark:data-[state=active]:bg-gray-800 data-[state=active]:shadow-sm transition-all duration-300"
              >
                <div className="flex items-center gap-2 px-2">
                  <MessageSquare className="w-4 h-4" />
                  <span className="text-sm font-medium">
                    {t("deals.reviews")}
                  </span>
                  <Badge className="ml-1 bg-yellow-600 text-white text-xs px-2 py-0.5">
                    {reviews.length}
                  </Badge>
                </div>
              </TabsTrigger>
            </TabsList>

            {/* Contenu des tabs */}
            <div className="flex-1 overflow-y-auto">
              <TabsContent value="details" className="px-6 py-6 space-y-6 m-0">
                <Form<CreateDealInput>
                  form={form}
                  groups={viewDetailDealFormGroups}
                  readOnly
                />
              </TabsContent>

              <TabsContent value="participants" className="px-6 py-6 m-0">
                <div className="space-y-6">
                  {/* Header avec statistiques */}
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div className="bg-gradient-to-br from-blue-50 to-blue-100/50 dark:from-blue-950/30 dark:to-blue-900/30 rounded-xl p-5 border border-blue-200/50 dark:border-blue-800/50 shadow-sm">
                      <div className="flex items-center gap-3 mb-2">
                        <div className="w-10 h-10 rounded-lg bg-white dark:bg-gray-800 shadow-sm flex items-center justify-center">
                          <Users className="w-5 h-5 text-blue-600 dark:text-blue-400" />
                        </div>
                        <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
                          {t("deals.participants")}
                        </p>
                      </div>
                      <p className="text-3xl font-bold text-blue-600 dark:text-blue-400">
                        {participants.length}
                      </p>
                      <p className="text-xs text-gray-600 dark:text-gray-400 mt-1">
                        {
                          participants.filter(
                            (p) => p.statutPaiement === StatutPaiement.CONFIRME,
                          ).length
                        }{" "}
                        {t("deals.confirmed")}
                      </p>
                    </div>

                    <div className="bg-gradient-to-br from-green-50 to-green-100/50 dark:from-green-950/30 dark:to-green-900/30 rounded-xl p-5 border border-green-200/50 dark:border-green-800/50 shadow-sm">
                      <div className="flex items-center gap-3 mb-2">
                        <div className="w-10 h-10 rounded-lg bg-white dark:bg-gray-800 shadow-sm flex items-center justify-center">
                          <Package className="w-5 h-5 text-green-600 dark:text-green-400" />
                        </div>
                        <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
                          {t("deals.partsSold")}
                        </p>
                      </div>
                      <p className="text-3xl font-bold text-green-600 dark:text-green-400">
                        {participants.reduce(
                          (sum, p) => sum + p.nombreDePart,
                          0,
                        )}
                        <span className="text-lg text-gray-500 dark:text-gray-400 font-normal">
                          /{partsTotal}
                        </span>
                      </p>
                      <p className="text-xs text-gray-600 dark:text-gray-400 mt-1">
                        {Math.round(
                          (participants.reduce(
                            (sum, p) => sum + p.nombreDePart,
                            0,
                          ) /
                            partsTotal) *
                            100,
                        )}
                        {t("deals.fillingRate")}
                      </p>
                    </div>

                    <div className="bg-gradient-to-br from-purple-50 to-purple-100/50 dark:from-purple-950/30 dark:to-purple-900/30 rounded-xl p-5 border border-purple-200/50 dark:border-purple-800/50 shadow-sm">
                      <div className="flex items-center gap-3 mb-2">
                        <div className="w-10 h-10 rounded-lg bg-white dark:bg-gray-800 shadow-sm flex items-center justify-center">
                          <TrendingUp className="w-5 h-5 text-purple-600 dark:text-purple-400" />
                        </div>
                        <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
                          {t("deals.totalRevenue")}
                        </p>
                      </div>
                      <p className="text-3xl font-bold text-purple-600 dark:text-purple-400">
                        {formatCurrency(
                          participants.reduce(
                            (sum, p) => sum + p.montantTotal,
                            0,
                          ),
                        )}
                      </p>
                      <p className="text-xs text-gray-600 dark:text-gray-400 mt-1">
                        {t("deals.collectedAmount")}
                      </p>
                    </div>
                  </div>

                  {/* Table des participants */}
                  <div className="bg-white dark:bg-gray-900 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden">
                    <div className="p-4 border-b border-gray-200 dark:border-gray-700 bg-gradient-to-r from-gray-50 to-blue-50/30 dark:from-gray-900 dark:to-blue-950/30">
                      <h3 className="text-base font-bold text-gray-900 dark:text-gray-100">
                        {t("deals.participantList")}
                      </h3>
                      <p className="text-sm text-gray-600 dark:text-gray-400 mt-0.5">
                        {t("deals.participantListDescription")}
                      </p>
                    </div>
                    <div className="p-4">
                      <DataTable<ParticipantDto, unknown>
                        columns={participantColumns}
                        data={participants}
                        enableSelection={false}
                        enableRowNumber={true}
                        enableExport={false}
                        enableSorting={true}
                        pageSizeOptions={[5, 10, 20]}
                      />
                    </div>
                  </div>
                </div>
              </TabsContent>

              <TabsContent value="supplier" className="px-6 py-6 m-0">
                <div className="space-y-6">
                  {/* Header fournisseur */}
                  <div className="bg-white dark:bg-gray-900 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6">
                    <div className="flex items-start gap-4">
                      <div className="w-16 h-16 rounded-xl bg-gradient-to-br from-blue-600 to-indigo-600 flex items-center justify-center shadow-lg flex-shrink-0">
                        <Store className="w-8 h-8 text-white" />
                      </div>
                      <div className="flex-1">
                        <h3 className="text-2xl font-bold text-gray-900 dark:text-gray-100 mb-2">
                          {supplierMock.name}
                        </h3>
                        <div className="flex items-center gap-2 mb-3">
                          <div className="flex items-center gap-1">
                            {Array.from({ length: 5 }).map((_, i) => (
                              <Star
                                key={i}
                                className={`w-4 h-4 ${
                                  i < Math.floor(supplierMock.rating)
                                    ? "fill-yellow-400 text-yellow-400"
                                    : "text-gray-300 dark:text-gray-600"
                                }`}
                              />
                            ))}
                          </div>
                          <span className="font-semibold text-gray-900 dark:text-gray-100">
                            {supplierMock.rating} / 5
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Informations du fournisseur */}
                  {/* <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="bg-gradient-to-br from-blue-50 to-blue-100/50 dark:from-blue-950/30 dark:to-blue-900/30 rounded-xl p-5 border border-blue-200/50 dark:border-blue-800/50 shadow-sm">
                      <div className="flex items-center gap-3 mb-3">
                        <div className="w-10 h-10 rounded-lg bg-white dark:bg-gray-800 shadow-sm flex items-center justify-center">
                          <MapPin className="w-5 h-5 text-blue-600 dark:text-blue-400" />
                        </div>
                        <p className="text-sm font-bold text-gray-900 dark:text-gray-100 uppercase tracking-wide">
                          {t("deals.location")}
                        </p>
                      </div>
                      <p className="text-xl font-bold text-gray-900 dark:text-gray-100">
                        {supplierMock.city}
                      </p>
                    </div>

                    <div className="bg-gradient-to-br from-green-50 to-green-100/50 dark:from-green-950/30 dark:to-green-900/30 rounded-xl p-5 border border-green-200/50 dark:border-green-800/50 shadow-sm">
                      <div className="flex items-center gap-3 mb-3">
                        <div className="w-10 h-10 rounded-lg bg-white dark:bg-gray-800 shadow-sm flex items-center justify-center">
                          <Phone className="w-5 h-5 text-green-600 dark:text-green-400" />
                        </div>
                        <p className="text-sm font-bold text-gray-900 dark:text-gray-100 uppercase tracking-wide">
                          {t("deals.contact")}
                        </p>
                      </div>
                      <p className="text-xl font-bold text-gray-900 dark:text-gray-100">
                        {supplierMock.contact}
                      </p>
                    </div>

                    <div className="bg-gradient-to-br from-purple-50 to-purple-100/50 dark:from-purple-950/30 dark:to-purple-900/30 rounded-xl p-5 border border-purple-200/50 dark:border-purple-800/50 shadow-sm">
                      <div className="flex items-center gap-3 mb-3">
                        <div className="w-10 h-10 rounded-lg bg-white dark:bg-gray-800 shadow-sm flex items-center justify-center">
                          <Package className="w-5 h-5 text-purple-600 dark:text-purple-400" />
                        </div>
                        <p className="text-sm font-bold text-gray-900 dark:text-gray-100 uppercase tracking-wide">
                          {t("deals.dealsPublished")}
                        </p>
                      </div>
                      <p className="text-3xl font-bold text-purple-600 dark:text-purple-400">
                        {supplierMock.dealsCount}
                      </p>
                      <p className="text-xs text-gray-600 dark:text-gray-400 mt-1">
                        {t("deals.offersAvailable")}
                      </p>
                    </div>

                    <div className="bg-gradient-to-br from-orange-50 to-orange-100/50 dark:from-orange-950/30 dark:to-orange-900/30 rounded-xl p-5 border border-orange-200/50 dark:border-orange-800/50 shadow-sm">
                      <div className="flex items-center gap-3 mb-3">
                        <div className="w-10 h-10 rounded-lg bg-white dark:bg-gray-800 shadow-sm flex items-center justify-center">
                          <Calendar className="w-5 h-5 text-orange-600 dark:text-orange-400" />
                        </div>
                        <p className="text-sm font-bold text-gray-900 dark:text-gray-100 uppercase tracking-wide">
                          {t("deals.memberSince")}
                        </p>
                      </div>
                      <p className="text-xl font-bold text-gray-900 dark:text-gray-100">
                        {new Date(supplierMock.joinedAt).toLocaleDateString(
                          "fr-FR",
                          {
                            year: "numeric",
                            month: "long",
                          },
                        )}
                      </p>
                    </div>
                  </div> */}
                </div>
              </TabsContent>

              <TabsContent value="reviews" className="px-6 py-6 m-0">
                <div className="space-y-6">
                  {/* Header avis */}
                  <div className="bg-gradient-to-br from-yellow-50 to-orange-50 dark:from-yellow-950/30 dark:to-orange-950/30 rounded-xl p-5 border border-yellow-200/50 dark:border-yellow-800/50 shadow-sm">
                    <div className="flex items-center gap-3">
                      <div className="w-12 h-12 rounded-xl bg-white dark:bg-gray-800 shadow-sm flex items-center justify-center">
                        <MessageSquare className="w-6 h-6 text-yellow-600 dark:text-yellow-400" />
                      </div>
                      <div>
                        <h3 className="text-lg font-bold text-gray-900 dark:text-gray-100">
                          {t("deals.reviews")}
                        </h3>
                        <p className="text-sm text-gray-600 dark:text-gray-400">
                          {t("deals.reviewsReceived", {
                            count: reviews.length,
                          })}
                        </p>
                      </div>
                    </div>
                  </div>

                  {/* Table des avis */}
                  <div className="bg-white dark:bg-gray-900 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden">
                    <div className="p-4">
                      <DataTable<ReviewRow, unknown>
                        columns={reviewColumns}
                        data={reviews}
                        enableSelection={false}
                        enableRowNumber={false}
                        enableExport={false}
                        enableSorting={true}
                        pageSizeOptions={[5, 10, 20]}
                      />
                    </div>
                  </div>
                </div>
              </TabsContent>
            </div>
          </Tabs>
        </div>
      </DialogContent>
    </Dialog>
  );
}
