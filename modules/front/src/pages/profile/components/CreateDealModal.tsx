import { Dialog, DialogContent } from "@/common/components/ui/dialog";
import Form, { type IFieldGroup } from "@/common/containers/Form";
import { DialogTitle } from "@radix-ui/react-dialog";
import { useCallback, useMemo, useState, type KeyboardEvent } from "react";
import { Button } from "@/common/components/ui/button";
import { getCroppedImg } from "@/common/utils/image";
import Cropper from "react-easy-crop";
import {
  Crop,
  ImageIcon,
  Plus,
  Sparkles,
  Upload,
  X,
  Star,
  Check,
  Loader2,
} from "lucide-react";
import { Input } from "@/common/components/ui/input";
import { HStack } from "@/common/components";
import {
  useCategories,
  useCreateDeal,
  useUsers,
  useUpdateDeal,
  type CreateDealDTO,
  type DealDTO,
  type UpdateDealDTO,
  useGetDealImageUrl,
} from "@/common/api";
import type { ImageResponse } from "@/common/api/hooks/useImageUpload";
import { toast } from "sonner";
import { dealSchema } from "@/common/schemas/deal.schema";
import { cn } from "@/common/utils";

/* ==============================
   Constants
============================== */

const MAX_IMAGES = 5;
const CROP_ASPECT = 4 / 3;

/* ==============================
   Helpers
============================== */

const buildPayload = (
  formData: any,
  now: string,
  connectedMerchantUuid?: string,
): CreateDealDTO => {
  const dateDebut = formData.dateDebut
    ? new Date(formData.dateDebut).toISOString().slice(0, 19)
    : now;
  const dateFin = formData.dateFin
    ? new Date(formData.dateFin).toISOString().slice(0, 19)
    : now;
  const dateExpiration = formData.dateExpiration
    ? new Date(formData.dateExpiration).toISOString().slice(0, 19)
    : dateFin;

  const [ville = "", pays = "CA"] = String(formData.location ?? "")
    .split(",")
    .map((s: string) => s.trim());

  const images = (formData.images ?? []) as (
    | ImageResponse
    | (Partial<ImageResponse> & { file: File })
  )[];

  return {
    titre: formData.title,
    description: formData.description,
    prixDeal:
      Number(formData.originalPrice) ||
      Number(formData.price) * Number(formData.partsTotal || 1),
    prixPart: Number(formData.price),
    nbParticipants: Number(formData.partsTotal),
    dateDebut,
    dateFin,
    dateExpiration,
    statut: formData.status,
    createurUuid: String(connectedMerchantUuid ?? formData.merchantId ?? ""),
    categorieUuid: String(formData.categoryId ?? ""),
    listePointsForts: String(formData.highlights ?? "")
      .split("\n")
      .map((s) => s.trim())
      .filter(Boolean),
    ville,
    pays,
    listeImages: images.map((img): Partial<ImageResponse> => {
      return {
        ...img,
        isPrincipal: !!img.isPrincipal,
      };
    }),
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

function ImagesField({
  field,
  form,
  dealUuid,
}: {
  field: any;
  form: any;
  dealUuid?: string;
}) {
  const [cropState, setCropState] = useState<{
    src: string;
    index: number;
  } | null>(null);

  const images =
    (form.watch(field.name) as (
      | ImageResponse
      | (Partial<ImageResponse> & { file: File })
    )[]) ?? [];
  const maxFiles = field.maxFiles ?? MAX_IMAGES;
  const canAddMore = images.length < maxFiles;

  const setImages = useCallback(
    (next: (ImageResponse | (Partial<ImageResponse> & { file: File }))[]) =>
      form.setValue(field.name, next, {
        shouldDirty: true,
        shouldValidate: true,
      }),
    [form, field.name],
  );

  const handleFileChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const files = Array.from(e.target.files ?? []);
      const remaining = maxFiles - images.length;
      const filesToAdd = files.slice(0, remaining);

      if (files.length > remaining) {
        toast.warning(
          `Vous ne pouvez ajouter que ${remaining} image(s) supplémentaire(s)`,
        );
      }

      const newImages = filesToAdd.map((file, index) => ({
        imageUuid: null,
        urlImage: file.name,
        nomUnique: file.name,
        isPrincipal: images.length === 0 && index === 0,
        statut: "PENDING" as const,
        file: file,
      }));

      setImages([...images, ...newImages]);
      e.target.value = "";
    },
    [images, maxFiles, setImages],
  );

  const removeImage = useCallback(
    (idx: number) => setImages(images.filter((_, i) => i !== idx)),
    [images, setImages],
  );

  const setPrincipalImage = useCallback(
    (idx: number) => {
      const newImages = images.map((img, i) => {
        if (i === idx) {
          return {
            ...img,
            isPrincipal: i === idx,
          };
        } else {
          return { ...img, isPrincipal: false };
        }
      });
      setImages(newImages);
      toast.success("Image principale mise à jour");
    },
    [images, setImages],
  );

  const openCrop = useCallback(
    (
      fileObj: ImageResponse | (Partial<ImageResponse> & { file: File }),
      index: number,
    ) => {
      if (fileObj.file) {
        setCropState({ src: URL.createObjectURL(fileObj.file), index });
      }
    },
    [],
  );

  const applyCrop = useCallback(
    async (croppedArea: any) => {
      if (!cropState || !croppedArea) return;
      const currentImageObj = images[cropState.index];
      if (!currentImageObj.file) return;

      const newFile = await getCroppedImg(
        cropState.src,
        croppedArea,
        currentImageObj.file,
      );
      const updated = [...images];
      updated[cropState.index] = {
        ...currentImageObj,
        file: newFile,
      };
      setImages(updated);
      setCropState(null);
    },
    [cropState, images, setImages],
  );

  return (
    <div className="space-y-6">
      {/* ... header ... */}
      <div className="flex items-center justify-between p-5 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl border-2 border-blue-100/50 shadow-sm">
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-xl bg-white shadow-sm flex items-center justify-center border border-blue-100">
            <ImageIcon className="w-5 h-5 text-blue-600" />
          </div>
          <div>
            <p className="text-sm font-bold text-gray-900">Galerie d'images</p>
            <p className="text-xs text-gray-600">
              JPG, PNG, WEBP • Max 5 MB par image
            </p>
          </div>
        </div>
        <div className="flex items-center gap-2 px-4 py-2 bg-white rounded-full shadow-sm border-2 border-blue-100">
          <span className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
            {images.length}
          </span>
          <span className="text-sm text-gray-500 font-medium">
            / {maxFiles}
          </span>
        </div>
      </div>

      {/* Upload Zone */}
      {canAddMore && (
        <label className="relative block cursor-pointer group">
          <input
            type="file"
            accept="image/*"
            multiple
            onChange={handleFileChange}
            className="sr-only"
          />
          <div className="border-2 border-dashed border-blue-200 rounded-2xl hover:border-blue-400 hover:bg-blue-50/30 transition-all duration-300 bg-white shadow-sm group-hover:shadow-md">
            <div className="flex flex-col items-center justify-center py-10 px-6">
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform duration-300 shadow-lg">
                <Upload className="w-8 h-8 text-white" />
              </div>
              <p className="text-base font-bold text-gray-900 mb-2">
                Cliquez ou glissez vos images ici
              </p>
              <p className="text-sm text-gray-600 text-center">
                Jusqu'à {maxFiles} images haute qualité
              </p>
            </div>
          </div>
        </label>
      )}

      {/* Images Grid */}
      {images.length > 0 ? (
        <div className="space-y-4">
          <div className="flex items-center justify-between px-1">
            <p className="text-sm font-bold text-gray-900">
              {images.length} image{images.length > 1 ? "s" : ""} sélectionnée
              {images.length > 1 ? "s" : ""}
            </p>
            <p className="text-xs text-blue-600 font-medium bg-blue-50 px-3 py-1 rounded-full">
              ⭐ Choisissez une image principale
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {images.map((img, idx) => {
              return (
                <ImageThumbnail
                  key={img.imageUuid || `${img.nomUnique}-${idx}`}
                  image={img}
                  index={idx}
                  dealUuid={dealUuid}
                  onRemove={() => removeImage(idx)}
                  onSetPrincipal={() => setPrincipalImage(idx)}
                  onCrop={() => openCrop(img, idx)}
                />
              );
            })}
          </div>
        </div>
      ) : (
        <div className="border-2 border-dashed border-gray-200 rounded-2xl py-16 bg-gradient-to-br from-gray-50 to-blue-50/30">
          <div className="flex flex-col items-center text-center">
            <div className="w-20 h-20 rounded-2xl bg-white shadow-md flex items-center justify-center mb-4 border-2 border-gray-100">
              <ImageIcon className="w-10 h-10 text-gray-300" />
            </div>
            <p className="text-base text-gray-900 font-bold mb-1">
              Aucune image ajoutée
            </p>
            <p className="text-sm text-gray-500">
              Cliquez sur la zone ci-dessus pour commencer
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

function ImageThumbnail({
  image,
  index,
  dealUuid,
  onRemove,
  onSetPrincipal,
  onCrop,
}: {
  image: ImageResponse | (Partial<ImageResponse> & { file: File });
  index: number;
  dealUuid?: string;
  onRemove: () => void;
  onSetPrincipal: () => void;
  onCrop: () => void;
}) {
  const isPrincipal = !!image.isPrincipal;
  const isNewFile = !!image.file;

  const { data: imageUrlData } = useGetDealImageUrl(
    dealUuid ?? "",
    !isNewFile && image.imageUuid ? image.imageUuid : "",
  );

  const src =
    isNewFile && image.file
      ? URL.createObjectURL(image.file)
      : imageUrlData?.url;

  return (
    <div
      className={cn(
        "relative aspect-[4/3] rounded-xl overflow-hidden group bg-white",
        "border-2 transition-all duration-300 shadow-sm hover:shadow-lg",
        isPrincipal
          ? "border-blue-500 ring-2 ring-blue-200/50 shadow-blue-100"
          : "border-gray-200 hover:border-blue-300",
      )}
    >
      {src ? (
        <img
          src={src}
          alt={`Upload ${index + 1}`}
          className="w-full h-full object-cover"
        />
      ) : (
        <div className="w-full h-full flex items-center justify-center bg-gray-100">
          <Loader2 className="w-6 h-6 text-gray-300 animate-spin" />
        </div>
      )}

      {/* Overlay gradient */}
      <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-all duration-300">
        {/* Actions buttons */}
        <div className="absolute bottom-0 left-0 right-0 p-3 flex items-center justify-between gap-2">
          <span className="text-white text-xs font-semibold truncate mr-2 bg-black/40 px-2 py-1 rounded-md backdrop-blur-sm">
            {image.nomUnique || (image.file && image.file.name)}
          </span>
          <div className="flex gap-1.5 flex-shrink-0">
            {!isPrincipal && (
              <Button
                type="button"
                size="sm"
                variant="secondary"
                className="h-8 w-8 p-0 bg-white hover:bg-blue-50 shadow-md"
                onClick={onSetPrincipal}
                title="Définir comme image principale"
              >
                <Star className="w-4 h-4 text-yellow-500" />
              </Button>
            )}
            {isNewFile && (
              <Button
                type="button"
                size="sm"
                variant="secondary"
                className="h-8 w-8 p-0 bg-white hover:bg-blue-50 shadow-md"
                onClick={onCrop}
                title="Recadrer"
              >
                <Crop className="w-4 h-4 text-gray-700" />
              </Button>
            )}
            <Button
              type="button"
              size="sm"
              colorScheme="danger"
              className="h-8 w-8 p-0 shadow-md"
              onClick={onRemove}
              title="Supprimer"
            >
              <X className="w-4 h-4" />
            </Button>
          </div>
        </div>

        {/* Badge principal */}
        {isPrincipal && (
          <div className="absolute top-3 left-3">
            <div className="flex items-center gap-1.5 bg-gradient-to-r from-blue-600 to-indigo-600 text-white text-xs font-bold px-3 py-1.5 rounded-full shadow-lg backdrop-blur-sm">
              <Star className="w-3.5 h-3.5 fill-current" />
              PRINCIPALE
            </div>
          </div>
        )}

        {/* Numéro */}
        <div className="absolute top-3 right-3">
          <div className="w-8 h-8 rounded-full bg-white flex items-center justify-center text-sm font-bold text-gray-700 shadow-lg">
            {index + 1}
          </div>
        </div>
      </div>

      {/* Badge principal visible sans hover */}
      {isPrincipal && (
        <div className="absolute top-3 left-3 group-hover:hidden">
          <div className="flex items-center gap-1.5 bg-gradient-to-r from-blue-600 to-indigo-600 text-white text-xs font-bold px-3 py-1.5 rounded-full shadow-lg">
            <Star className="w-3.5 h-3.5 fill-current" />
            PRINCIPALE
          </div>
        </div>
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
      <HStack className="flex gap-2 w-full">
        <Input
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Ex: Menu gastronomique 3 plats..."
          wrapperClassName="flex-1"
        />
        <Button
          type="button"
          onClick={addHighlight}
          disabled={!inputValue.trim()}
          size="sm"
          className="bg-blue-600 hover:bg-blue-700 text-white shadow-sm"
        >
          <Plus className="w-4 h-4 mr-1.5" />
          Ajouter
        </Button>
      </HStack>

      {highlights.length > 0 ? (
        <div className="space-y-3">
          <p className="text-xs font-semibold text-gray-700 uppercase tracking-wide">
            {highlights.length} point{highlights.length > 1 ? "s" : ""} fort
            {highlights.length > 1 ? "s" : ""} ajouté
            {highlights.length > 1 ? "s" : ""}
          </p>
          <div className="flex flex-wrap gap-2">
            {highlights.map((highlight: string, idx: number) => (
              <div
                key={idx}
                className="inline-flex items-center gap-2 px-4 py-2.5 bg-gradient-to-r from-blue-50 to-indigo-50 text-blue-700 border border-blue-200 rounded-lg text-sm font-medium group hover:from-blue-100 hover:to-indigo-100 hover:shadow-sm transition-all duration-200"
              >
                <Sparkles className="w-4 h-4 text-blue-500" />
                <span>{highlight}</span>
                <button
                  type="button"
                  onClick={() => removeHighlight(idx)}
                  className="ml-1 hover:bg-blue-200/60 rounded-md p-1 transition-colors"
                  title="Supprimer ce point fort"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              </div>
            ))}
          </div>
        </div>
      ) : (
        <div className="border-2 border-dashed border-gray-200 rounded-xl py-8 px-4 bg-gray-50/50">
          <div className="flex flex-col items-center text-center">
            <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-gray-100 to-gray-200 flex items-center justify-center mb-3">
              <Sparkles className="w-7 h-7 text-gray-400" />
            </div>
            <p className="text-sm text-gray-600 font-semibold mb-1">
              Aucun point fort ajouté
            </p>
            <p className="text-xs text-gray-500">
              Ajoutez les avantages de votre offre
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
  initialData,
  connectedMerchantUuid,
}: {
  open: boolean;
  onClose: () => void;
  onSuccess?: () => void;
  initialData?: DealDTO | null;
  connectedMerchantUuid?: string;
}) {
  const { data: categoriesData } = useCategories();
  const { data: usersData } = useUsers();
  const {
    mutateAsync: createDeal,
    isPending: isCreating,
    isUploading,
    progress,
    hasErrors,
  } = useCreateDeal();

  const {
    mutateAsync: updateDeal,
    isPending: isUpdatingEnhanced,
    isUploading: isUploadingUpdate,
    progress: updateProgressMap,
    hasErrors: updateHasErrors,
  } = useUpdateDeal();

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
    () => [
      { label: "📝 Brouillon", value: "BROUILLON" },
      { label: "✅ Publié", value: "PUBLIE" },
    ],
    [],
  );
  const hideMerchantField = Boolean(connectedMerchantUuid);

  // Calculer la progression globale des uploads
  const uploadProgress = useMemo(() => {
    const activeProgress = isUploading ? progress : updateProgressMap;
    if (!activeProgress || activeProgress.size === 0) return null;

    const progressArray = Array.from(activeProgress.values());
    const totalProgress = progressArray.reduce((sum, p) => sum + p.progress, 0);
    const avgProgress = totalProgress / progressArray.length;
    const completed = progressArray.filter(
      (p) => p.status === "success",
    ).length;
    const failed = progressArray.filter((p) => p.status === "error").length;

    return {
      percentage: Math.round(avgProgress),
      completed,
      failed,
      total: progressArray.length,
    };
  }, [progress, updateProgressMap, isUploading]);

  const handleSubmit = useCallback(
    async ({ data }: { data: any }) => {
      try {
        const now = new Date().toISOString().slice(0, 19);

        if (initialData) {
          // 1) Préparer le payload de mise à jour
          const { images, ...formData } = data;
          const basePayload = buildPayload(
            formData,
            now,
            connectedMerchantUuid,
          );

          const payload: UpdateDealDTO = {
            titre: basePayload.titre,
            description: basePayload.description,
            prixDeal: basePayload.prixDeal,
            prixPart: basePayload.prixPart,
            nbParticipants: basePayload.nbParticipants,
            dateDebut: basePayload.dateDebut,
            dateFin: basePayload.dateFin,
            dateExpiration: basePayload.dateExpiration,
            categorieUuid: basePayload.categorieUuid,
            listePointsForts: basePayload.listePointsForts,
            ville: basePayload.ville,
            pays: basePayload.pays,
            createurUuid: basePayload.createurUuid,
          };

          // 2) Gérer la logique des images
          const currentImages = initialData.listeImages || [];
          const formImages = (images || []) as ImageResponse[];

          // Vérifier si les images ont été modifiées
          const isSameLength = currentImages.length === formImages.length;
          const isSame =
            JSON.stringify(currentImages) === JSON.stringify(formImages);
          const isSameContent =
            isSame &&
            isSameLength &&
            formImages.every((img, idx) => {
              const current = currentImages[idx];
              if (img instanceof File) return false;
              return img.imageUuid === current.imageUuid;
            });

          if (!isSameContent) {
            payload.listeImages = formImages.map((img, index) => {
              if (img.file instanceof File) {
                return {
                  imageUuid: null, // Nouvelles images : imageUuid null
                  urlImage: img.nomUnique,
                  nomUnique: img.nomUnique,
                  isPrincipal: img.isPrincipal || index === 0,
                  statut: "PENDING" as any,
                  file: img.file,
                };
              }

              // Images existantes conservées
              return {
                ...img,
              };
            });
          }

          // 3) Appel unique pour la mise à jour
          await updateDeal({ id: initialData.uuid, data: payload });

          if (updateHasErrors) {
            toast.warning("Deal mis à jour avec des erreurs d'upload", {
              description:
                "Certaines nouvelles images n'ont pas pu être téléchargées",
            });
          } else {
            toast.success("✅ Deal mis à jour avec succès");
          }
        } else {
          // Create new deal
          await createDeal(buildPayload(data, now, connectedMerchantUuid));

          if (hasErrors) {
            toast.warning("Deal créé avec des erreurs d'upload", {
              description: "Certaines images n'ont pas pu être téléchargées",
            });
          } else {
            toast.success("✨ Deal créé avec succès!", {
              description: "Toutes les images ont été téléchargées",
            });
          }
        }

        onSuccess?.();
        onClose();
      } catch (error: any) {
        toast.error(
          initialData
            ? "❌ Erreur lors de la mise à jour du deal"
            : "❌ Erreur lors de la création du deal",
          {
            description: error?.response?.data?.message || error?.message,
          },
        );
      }
    },
    [
      createDeal,
      updateDeal,
      onSuccess,
      onClose,
      hasErrors,
      updateHasErrors,
      initialData,
      connectedMerchantUuid,
    ],
  );

  const formGroups: IFieldGroup[] = useMemo(
    () => [
      {
        title: "📋 Informations générales",
        description: "Donnez vie à votre offre avec un titre accrocheur",
        columns: 1,
        className: "bg-white rounded-xl shadow-sm border border-gray-100 p-6",
        fields: [
          {
            type: "text",
            name: "title",
            label: "Titre du deal",
            placeholder:
              "Ex : Dîner romantique pour 2 personnes au restaurant étoilé",
            colSpan: 1,
          },
          {
            type: "textarea",
            name: "description",
            label: "Description complète",
            colSpan: 1,
            placeholder:
              "Décrivez votre offre de manière détaillée et attractive...",
            rows: 4,
          },
        ],
      },
      {
        title: "🏷️ Catégorie & Statut",
        description: "Classez votre offre et définissez sa visibilité",
        columns: 2,
        className: "bg-white rounded-xl shadow-sm border border-gray-100 p-6",
        fields: [
          {
            type: "select",
            name: "categoryId",
            label: "Catégorie",
            placeholder: "Choisir une catégorie",
            items: categoryItems,
          },
          {
            type: "select",
            name: "status",
            label: "Statut de publication",
            placeholder: "Choisir le statut",
            items: statusItems,
          },
        ],
      },
      {
        title: "💰 Tarification",
        description: "Définissez le prix et la valeur de votre offre",
        columns: 3,
        className: "bg-white rounded-xl shadow-sm border border-gray-100 p-6",
        fields: [
          {
            type: "number",
            name: "price",
            label: "Prix par part",
            placeholder: "0.00",
            prefix: "$",
          },
          {
            type: "number",
            name: "originalPrice",
            label: "Prix initial",
            placeholder: "0.00",
            prefix: "$",
          },
          {
            type: "number",
            name: "partsTotal",
            label: "Nombre de parts",
            placeholder: "0",
          },
        ],
      },
      {
        title: "📅 Disponibilité",
        description: "Configurez les dates de début, de fin et d'expiration",
        columns: 3,
        className: "bg-white rounded-xl shadow-sm border border-gray-100 p-6",
        fields: [
          {
            type: "date",
            name: "dateDebut",
            label: "Date de début",
          },
          {
            type: "date",
            name: "dateFin",
            label: "Date de fin",
          },
          {
            type: "date",
            name: "dateExpiration",
            label: "Date d'expiration",
          },
        ],
      },
      {
        title: "📍 Localisation",
        description: "Où se déroule votre offre ?",
        columns: 1,
        className: "bg-white rounded-xl shadow-sm border border-gray-100 p-6",
        fields: [
          {
            type: "text",
            name: "location",
            label: "Lieu",
            placeholder: "Ville, Quartier (Ex: Douala – Bonapriso)",
          },
        ],
      },
      {
        title: "✨ Points forts de l'offre",
        description: "Mettez en avant ce qui rend votre offre unique",
        columns: 1,
        className: "bg-white rounded-xl shadow-sm border border-gray-100 p-6",
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
        title: "👤 Fournisseur",
        description: "Qui propose cette offre ?",
        columns: 1,
        className: "bg-white rounded-xl shadow-sm border border-gray-100 p-6",
        hidden: hideMerchantField,
        fields: [
          {
            type: "select" as const,
            name: "merchantId",
            label: "Nom du fournisseur",
            placeholder: "Sélectionner un fournisseur",
            items: userItems,
          },
        ],
      },
      {
        title: "📸 Galerie d'images",
        description: "Ajoutez jusqu'à 5 images attractives pour votre offre",
        className: "bg-white rounded-xl shadow-sm border border-gray-100 p-6",
        fields: [
          {
            type: "file" as const,
            name: "images",
            label: "Images",
            maxFiles: MAX_IMAGES,
            render: (field, form) => (
              <ImagesField
                field={field}
                form={form}
                dealUuid={initialData?.uuid}
              />
            ),
          },
        ],
      },
    ],
    [categoryItems, statusItems, userItems, hideMerchantField, initialData],
  );

  const defaultValues = useMemo(() => {
    const parseDate = (dateStr?: any) => {
      if (!dateStr) return undefined;
      // Handle LocalDateTime format from Java (without Z or offset)
      // Some browsers might need "T" instead of space, but Java LocalDateTime already has T
      // We also ensure it's not already a Date object
      if (dateStr instanceof Date) return dateStr;

      const date = new Date(dateStr);
      if (isNaN(date.getTime())) {
        console.warn("Invalid date format:", dateStr);
        return undefined;
      }
      return date;
    };

    if (initialData) {
      // DEBUG: console.log("InitialData dates:", initialData.dateDebut, initialData.dateFin, initialData.dateExpiration);
      const parsedValues = {
        title: initialData.titre,
        description: initialData.description,
        categoryId: initialData.categorieUuid,
        status: initialData.statut,
        price: initialData.prixPart,
        originalPrice: initialData.prixDeal,
        partsTotal: initialData.nbParticipants,
        dateDebut: parseDate(initialData.dateDebut),
        dateFin: parseDate(initialData.dateFin),
        dateExpiration: parseDate(initialData.dateExpiration),
        location: initialData.ville
          ? `${initialData.ville}, ${initialData.pays}`
          : undefined,
        highlights: (initialData.listePointsForts || []).join("\n"),
        merchantId: connectedMerchantUuid ?? initialData.createurUuid,
        images: initialData.listeImages || [],
      };
      // DEBUG: console.log("Parsed defaultValues:", parsedValues);
      return parsedValues;
    }

    if (connectedMerchantUuid) {
      return { merchantId: connectedMerchantUuid };
    }

    return undefined;
  }, [initialData, connectedMerchantUuid]);

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent
        size="xl"
        className="p-0 h-[90vh] overflow-hidden bg-gradient-to-br from-gray-50 to-blue-50/30"
      >
        <DialogTitle className="px-6 py-5 border-b border-gray-200 bg-white shadow-sm">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-blue-600 to-indigo-600 flex items-center justify-center shadow-lg">
              <Sparkles className="w-6 h-6 text-white" />
            </div>
            <div className="flex-1">
              <h2 className="text-xl font-bold text-gray-900">
                {initialData ? "Modifier le deal" : "Créer un nouveau deal"}
              </h2>
              <p className="text-sm text-gray-600 font-normal mt-0.5">
                {initialData
                  ? "Mettre à jour les informations de l'offre"
                  : "Remplissez les informations pour publier votre offre exceptionnelle"}
              </p>
            </div>
          </div>
        </DialogTitle>

        {/* Indicateur de progression d'upload */}
        {(isUploading || isUploadingUpdate) && uploadProgress && (
          <div className="px-6 py-4 bg-gradient-to-r from-blue-50 to-indigo-50 border-b border-blue-200/50 shadow-sm">
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Loader2 className="w-5 h-5 text-blue-600 animate-spin" />
                  <span className="text-sm font-bold text-gray-900">
                    Upload des images en cours...
                  </span>
                </div>
                <span className="text-base font-bold text-blue-600">
                  {uploadProgress.percentage}%
                </span>
              </div>

              <div className="w-full bg-white rounded-full h-3 overflow-hidden shadow-inner">
                <div
                  className="bg-gradient-to-r from-blue-500 via-blue-600 to-indigo-600 h-3 rounded-full transition-all duration-500 ease-out shadow-sm"
                  style={{ width: `${uploadProgress.percentage}%` }}
                />
              </div>

              <div className="flex items-center justify-between text-xs">
                <div className="flex items-center gap-4">
                  <span className="flex items-center gap-1.5 text-green-700 font-semibold bg-green-50 px-2.5 py-1 rounded-full">
                    <Check className="w-3.5 h-3.5" />
                    {uploadProgress.completed} réussie(s)
                  </span>
                  {uploadProgress.failed > 0 && (
                    <span className="flex items-center gap-1.5 text-red-700 font-semibold bg-red-50 px-2.5 py-1 rounded-full">
                      <X className="w-3.5 h-3.5" />
                      {uploadProgress.failed} échouée(s)
                    </span>
                  )}
                </div>
                <span className="text-gray-600 font-medium">
                  {uploadProgress.completed + uploadProgress.failed} /{" "}
                  {uploadProgress.total}
                </span>
              </div>
            </div>
          </div>
        )}

        <div className="h-[calc(90vh-88px)] overflow-y-auto px-6 py-6">
          <Form<CreateDealDTO>
            groups={formGroups}
            schema={dealSchema}
            defaultValues={defaultValues as any}
            submitLabel={
              isCreating ||
              isUploading ||
              isUpdatingEnhanced ||
              isUploadingUpdate
                ? isUploading || isUploadingUpdate
                  ? "⏳ Upload des images..."
                  : isUpdatingEnhanced
                    ? "⏳ Mise à jour en cours..."
                    : "⏳ Création en cours..."
                : initialData
                  ? "🔁 Mettre à jour"
                  : "✨ Créer le deal"
            }
            onSubmit={handleSubmit}
            onError={(errors) => {
              console.error("Form validation errors:", errors);
              toast.error("Veuillez corriger les erreurs dans le formulaire");
            }}
            isLoading={
              isCreating ||
              isUploading ||
              isUpdatingEnhanced ||
              isUploadingUpdate
            }
          />
        </div>
      </DialogContent>
    </Dialog>
  );
}
