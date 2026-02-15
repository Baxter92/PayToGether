import { Dialog, DialogContent } from "@/common/components/ui/dialog";
import Form, { type IFieldGroup } from "@/common/containers/Form";
import { DialogTitle } from "@radix-ui/react-dialog";
import { useCallback, useMemo, useState, type KeyboardEvent } from "react";
import { Button } from "@/common/components/ui/button";
import { getCroppedImg } from "@/common/utils/image";
import Cropper from "react-easy-crop";
import { Crop, ImageIcon, Plus, Sparkles, Upload, X } from "lucide-react";
import { Input } from "@/common/components/ui/input";
import { HStack } from "@/common/components";
import {
  StatutDeal,
  useCategories,
  useCreateDeal,
  useUsers,
  type CreateDealDTO,
} from "@/common/api";
import type { ImageResponse } from "@/common/api/hooks/useImageUpload";
import { toast } from "sonner";
import { dealSchema } from "@/common/schemas/deal.schema";

/* ==============================
   Constants
============================== */

const MAX_IMAGES = 5;
const CROP_ASPECT = 4 / 3;

/* ==============================
   Helpers
============================== */

const buildPayload = (formData: any, now: string): CreateDealDTO => {
  const expiration = formData.expiryDate
    ? new Date(formData.expiryDate).toISOString().slice(0, 19)
    : now;

  const [ville = "", pays = "CM"] = String(formData.location ?? "")
    .split(",")
    .map((s: string) => s.trim());

  const images = (formData.images ?? []) as File[];

  return {
    titre: formData.title,
    description: formData.description,
    prixDeal:
      Number(formData.originalPrice) ||
      Number(formData.price) * Number(formData.partsTotal || 1),
    prixPart: Number(formData.price),
    nbParticipants: Number(formData.partsTotal),
    dateDebut: now,
    dateFin: expiration,
    dateExpiration: expiration,
    statut: formData.status,
    createurUuid: String(formData.merchantId ?? ""),
    categorieUuid: String(formData.categoryId ?? ""),
    listePointsForts: String(formData.highlights ?? "")
      .split("\n")
      .map((s) => s.trim())
      .filter(Boolean),
    ville,
    pays,
    listeImages: images.map(
      (file, index): Partial<ImageResponse> => ({
        urlImage: file.name,
        nomUnique: file.name,
        statut: "PENDING",
        isPrincipal: index === 0,
        presignUrl: null,
        file,
      }),
    ),
  };
};

/* ==============================
   CropModal
============================== */

function CropModal({
  src,
  onClose,
  onApply,
}: {
  src: string;
  onClose: () => void;
  onApply: (area: any) => void;
}) {
  const [crop, setCrop] = useState({ x: 0, y: 0 });
  const [zoom, setZoom] = useState(1);
  const [croppedArea, setCroppedArea] = useState<any>(null);

  const onCropComplete = useCallback((_: any, pixels: any) => {
    setCroppedArea(pixels);
  }, []);

  return (
    <Dialog open onOpenChange={onClose}>
      <DialogContent className="max-w-3xl">
        <div className="space-y-4">
          <div>
            <h3 className="text-lg font-semibold">Recadrer l'image</h3>
            <p className="text-sm text-muted-foreground">
              Ajustez le cadrage et le zoom de votre image
            </p>
          </div>

          <div className="relative h-[400px] bg-gray-900 rounded-lg overflow-hidden">
            <Cropper
              image={src}
              crop={crop}
              zoom={zoom}
              aspect={CROP_ASPECT}
              onCropChange={setCrop}
              onCropComplete={onCropComplete}
              onZoomChange={setZoom}
            />
          </div>

          <div>
            <label className="text-sm font-medium mb-2 block">
              Zoom: {Math.round(zoom * 100)}%
            </label>
            <input
              type="range"
              min={1}
              max={3}
              step={0.1}
              value={zoom}
              onChange={(e) => setZoom(Number(e.target.value))}
              className="w-full"
            />
          </div>

          <div className="flex justify-end gap-3 pt-4 border-t">
            <Button type="button" variant="outline" onClick={onClose}>
              Annuler
            </Button>
            <Button
              type="button"
              onClick={() => onApply(croppedArea)}
              disabled={!croppedArea}
            >
              Appliquer le recadrage
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}

/* ==============================
   ImagesField
============================== */

function ImagesField({ field, form }: { field: any; form: any }) {
  const [cropState, setCropState] = useState<{
    src: string;
    index: number;
  } | null>(null);

  const images = (form.watch(field.name) as File[]) ?? [];
  const maxFiles = field.maxFiles ?? MAX_IMAGES;
  const canAddMore = images.length < maxFiles;

  const setImages = useCallback(
    (next: File[]) =>
      form.setValue(field.name, next, {
        shouldDirty: true,
        shouldValidate: true,
      }),
    [form, field.name],
  );

  const handleFileChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const files = Array.from(e.target.files ?? []);
      setImages([...images, ...files].slice(0, maxFiles));
      e.target.value = "";
    },
    [images, maxFiles, setImages],
  );

  const removeImage = useCallback(
    (idx: number) => setImages(images.filter((_, i) => i !== idx)),
    [images, setImages],
  );

  const openCrop = useCallback((file: File, index: number) => {
    setCropState({ src: URL.createObjectURL(file), index });
  }, []);

  const applyCrop = useCallback(
    async (croppedArea: any) => {
      if (!cropState || !croppedArea) return;
      const newFile = await getCroppedImg(
        cropState.src,
        croppedArea,
        images[cropState.index],
      );
      const updated = [...images];
      updated[cropState.index] = newFile;
      setImages(updated);
      setCropState(null);
    },
    [cropState, images, setImages],
  );

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <p className="text-xs text-muted-foreground">
          Maximum {maxFiles} images • Format: JPG, PNG, WEBP
        </p>
        <span className="text-xs text-muted-foreground font-medium">
          {images.length} / {maxFiles}
        </span>
      </div>

      {canAddMore && (
        <label className="relative block cursor-pointer">
          <input
            type="file"
            accept="image/*"
            multiple
            onChange={handleFileChange}
            className="sr-only"
          />
          <div className="border-2 border-dashed border-gray-300 rounded-lg hover:border-gray-400 transition-colors group">
            <div className="flex flex-col items-center justify-center py-8 px-4">
              <div className="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center mb-3 group-hover:bg-gray-200 transition-colors">
                <Upload className="w-5 h-5 text-gray-600" />
              </div>
              <p className="text-sm font-medium text-gray-700 mb-1">
                Cliquez pour télécharger
              </p>
              <p className="text-xs text-gray-500">
                PNG, JPG ou WEBP (max. 5MB par image)
              </p>
            </div>
          </div>
        </label>
      )}

      {images.length > 0 && (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          {images.map((file, idx) => (
            <div
              key={idx}
              className="relative aspect-square rounded-lg overflow-hidden border border-gray-200 group bg-gray-50"
            >
              <img
                src={URL.createObjectURL(file)}
                alt={`Upload ${idx + 1}`}
                className="w-full h-full object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity">
                <div className="absolute bottom-0 left-0 right-0 p-3 flex items-center justify-between">
                  <span className="text-white text-xs font-medium truncate mr-2">
                    Image {idx + 1}
                  </span>
                  <div className="flex gap-1.5">
                    <Button
                      type="button"
                      size="sm"
                      variant="secondary"
                      className="h-8 w-8 p-0"
                      onClick={() => openCrop(file, idx)}
                    >
                      <Crop className="w-4 h-4" />
                    </Button>
                    <Button
                      type="button"
                      size="sm"
                      colorScheme="danger"
                      className="h-8 w-8 p-0"
                      onClick={() => removeImage(idx)}
                    >
                      <X className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
                {idx === 0 && (
                  <div className="absolute top-2 left-2">
                    <span className="bg-blue-600 text-white text-xs font-medium px-2 py-1 rounded">
                      Principale
                    </span>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {images.length === 0 && !canAddMore && (
        <div className="border border-gray-200 rounded-lg py-12">
          <div className="flex flex-col items-center text-center">
            <div className="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center mb-4">
              <ImageIcon className="w-8 h-8 text-gray-400" />
            </div>
            <p className="text-sm text-gray-600 font-medium">
              Aucune image ajoutée
            </p>
            <p className="text-xs text-gray-500 mt-1">
              Ajoutez au moins une image pour continuer
            </p>
          </div>
        </div>
      )}

      {cropState && (
        <CropModal
          src={cropState.src}
          onClose={() => setCropState(null)}
          onApply={applyCrop}
        />
      )}
    </div>
  );
}

/* ==============================
   HighlightsField
============================== */

function HighlightsField({ field, form }: { field: any; form: any }) {
  const [inputValue, setInputValue] = useState("");

  const highlightsString = form.watch(field.name) as string | undefined;
  const highlights = useMemo(
    () =>
      highlightsString
        ? highlightsString.split("\n").filter((h: string) => h.trim())
        : [],
    [highlightsString],
  );

  const setHighlights = useCallback(
    (next: string[]) =>
      form.setValue(field.name, next.join("\n"), {
        shouldDirty: true,
        shouldValidate: true,
      }),
    [form, field.name],
  );

  const addHighlight = useCallback(() => {
    if (!inputValue.trim()) return;
    setHighlights([...highlights, inputValue.trim()]);
    setInputValue("");
  }, [inputValue, highlights, setHighlights]);

  const removeHighlight = useCallback(
    (index: number) => setHighlights(highlights.filter((_, i) => i !== index)),
    [highlights, setHighlights],
  );

  const handleKeyDown = useCallback(
    (e: KeyboardEvent<HTMLInputElement>) => {
      if (e.key === "Enter") {
        e.preventDefault();
        addHighlight();
      }
    },
    [addHighlight],
  );

  return (
    <div className="space-y-4">
      <div>
        <label className="text-sm font-medium block mb-1">{field.label}</label>
        <p className="text-xs text-muted-foreground">
          Ajoutez les points forts de votre offre
        </p>
      </div>

      <HStack className="flex gap-2 w-full">
        <Input
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Ex: Menu gastronomique..."
          wrapperClassName="flex-1"
        />
        <Button
          type="button"
          onClick={addHighlight}
          disabled={!inputValue.trim()}
          size="sm"
        >
          <Plus className="w-4 h-4 mr-1" />
          Ajouter
        </Button>
      </HStack>

      {highlights.length > 0 ? (
        <div className="space-y-2">
          <p className="text-xs font-medium text-gray-700">
            Points forts ajoutés:
          </p>
          <div className="flex flex-wrap gap-2">
            {highlights.map((highlight: string, idx: number) => (
              <div
                key={idx}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-blue-50 text-blue-700 border border-blue-200 rounded-full text-sm font-medium group hover:bg-blue-100 transition-colors"
              >
                <Sparkles className="w-3.5 h-3.5" />
                <span>{highlight}</span>
                <button
                  type="button"
                  onClick={() => removeHighlight(idx)}
                  className="ml-1 hover:bg-blue-200 rounded-full p-0.5 transition-colors"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              </div>
            ))}
          </div>
        </div>
      ) : (
        <div className="border border-dashed border-gray-300 rounded-lg py-2 px-4">
          <div className="flex flex-col items-center text-center">
            <div className="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center mb-3">
              <Sparkles className="w-6 h-6 text-gray-400" />
            </div>
            <p className="text-sm text-gray-600 font-medium">
              Aucun point fort ajouté
            </p>
          </div>
        </div>
      )}
    </div>
  );
}

/* ==============================
   CreateDealModal
============================== */

export function CreateDealModal({
  open,
  onClose,
  onSuccess,
}: {
  open: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}) {
  const { data: categoriesData } = useCategories();
  const { data: usersData } = useUsers();
  const {
    mutateAsync: createDeal,
    isPending: isCreating,
    isUploading,
  } = useCreateDeal();

  const categoryItems = useMemo(
    () => (categoriesData ?? []).map((c) => ({ label: c.nom, value: c.uuid })),
    [categoriesData],
  );

  const userItems = useMemo(
    () =>
      (usersData ?? []).map((user: any) => ({
        label:
          [user?.prenom, user?.nom].filter(Boolean).join(" ").trim() ||
          user?.email ||
          user?.uuid,
        value: user?.uuid,
      })),
    [usersData],
  );

  const statusItems = useMemo(
    () =>
      Object.entries(StatutDeal).map(([key, value]) => ({
        label: value,
        value: key,
      })),
    [],
  );

  const handleSubmit = useCallback(
    async ({ data }: { data: any }) => {
      try {
        const now = new Date().toISOString().slice(0, 19);
        await createDeal(buildPayload(data, now));
        toast.success("Deal créé avec succès");
        onSuccess?.();
        // onClose();
      } catch (error: any) {
        toast.error("Erreur lors de la création du deal", {
          description: error?.response?.data?.message || error?.message,
        });
      }
    },
    [createDeal, onSuccess, onClose],
  );

  const formGroups: IFieldGroup[] = useMemo(
    () => [
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
            colSpan: 2,
          },
          {
            type: "textarea",
            name: "description",
            label: "Description complète",
            colSpan: 2,
            placeholder: "Décrivez l'offre en détail...",
          },
          {
            type: "select",
            name: "categoryId",
            label: "Catégorie",
            items: categoryItems,
          },
          {
            type: "radio",
            name: "status",
            label: "Statut du deal",
            items: statusItems,
          },
        ],
      },
      {
        title: "Tarification",
        columns: 2,
        fields: [
          { type: "number", name: "price", label: "Prix de la part (USD)" },
          {
            type: "number",
            name: "originalPrice",
            label: "Prix initial (USD)",
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
          { type: "date", name: "expiryDate", label: "Date d'expiration" },
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
        title: "Contenu de l'offre",
        columns: 1,
        fields: [
          {
            type: "textarea",
            name: "highlights",
            label: "Points forts",
            render: (field, form) => (
              <HighlightsField field={field} form={form} />
            ),
          },
        ],
      },
      {
        title: "Fournisseur & logistique",
        columns: 1,
        fields: [
          {
            type: "select" as const,
            name: "merchantId",
            label: "Nom du fournisseur",
            items: userItems,
          },
        ],
      },
      {
        title: "Images du deal",
        description: "Ajoutez des images attractives",
        fields: [
          {
            type: "file" as const,
            name: "images",
            label: "Images",
            maxFiles: MAX_IMAGES,
            render: (field, form) => <ImagesField field={field} form={form} />,
          },
        ],
      },
    ],
    [categoryItems, statusItems, userItems],
  );

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent size="xl" className="p-0 h-[90vh] overflow-hidden">
        <DialogTitle className="px-6 py-4 border-b font-semibold">
          Ajouter un nouveau deal
        </DialogTitle>
        <div className="h-[calc(90vh-72px)] overflow-y-auto px-6 py-4">
          <Form<CreateDealDTO>
            groups={formGroups}
            schema={dealSchema}
            submitLabel="Créer le deal"
            onSubmit={handleSubmit}
            isLoading={isCreating || isUploading}
          />
        </div>
      </DialogContent>
    </Dialog>
  );
}
