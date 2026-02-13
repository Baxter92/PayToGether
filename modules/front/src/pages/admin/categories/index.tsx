import {type ReactElement, useState } from "react";
import {
  FolderTree,
  Plus,
  MoreHorizontal,
  Pencil,
  Trash2,
  X,
} from "lucide-react";
import { useI18n } from "@/common/hooks/useI18n";
import { Button } from "@/common/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/common/components/ui/card";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/common/components/ui/dropdown-menu";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/common/components/ui/dialog";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/common/components/ui/alert-dialog";
import Form from "@/common/containers/Form";
import { toast } from "sonner";
import { categorySchema } from "@/common/schemas/category.schema";
import {
  useCategories,
  useCreateCategorie,
  useDeleteCategorie,
  useUpdateCategorie,
  type CategoryDTO,
  type CreateCategoryDTO,
  type UpdateCategoryDTO,
} from "@/common/api";

type CategoryFormMode = "create" | "edit" | null;

export default function AdminCategories(): ReactElement {
  const [formMode, setFormMode] = useState<CategoryFormMode>(null);
  const [selectedCategory, setSelectedCategory] = useState<CategoryDTO | null>(
    null,
  );
  const [categoryToDelete, setCategoryToDelete] = useState<CategoryDTO | null>(
    null,
  );

  const { t: tAdmin } = useI18n("admin");

  const { data: categories, isLoading: isLoadingCategories } = useCategories();
  const { mutateAsync: createCategorie, isPending: isCreating } =
    useCreateCategorie();
  const { mutateAsync: updateCategorie, isPending: isUpdating } =
    useUpdateCategorie();
  const { mutateAsync: deleteCategorie, isPending: isDeleting } =
    useDeleteCategorie();

  const isFormOpen = formMode !== null;
  const isLoading = isCreating || isUpdating;

  // Ouvrir le formulaire en mode création
  const handleOpenCreate = () => {
    setSelectedCategory(null);
    setFormMode("create");
  };

  // Ouvrir le formulaire en mode édition
  const handleOpenEdit = (category: CategoryDTO) => {
    setSelectedCategory(category);
    setFormMode("edit");
  };

  // Fermer le formulaire
  const handleCloseForm = () => {
    setFormMode(null);
    setSelectedCategory(null);
  };

  // Soumettre le formulaire (création ou modification)
  const handleSubmitForm = async (
    data: CreateCategoryDTO | UpdateCategoryDTO,
  ) => {
    try {
      if (formMode === "create") {
        await createCategorie(data as CreateCategoryDTO);
        toast.success("Catégorie créée", {
          description: `La catégorie "${data.nom}" a été créée avec succès`,
        });
      } else if (formMode === "edit" && selectedCategory) {
        await updateCategorie({
          id: selectedCategory.uuid,
          data: data as UpdateCategoryDTO,
        });
        toast.success("Catégorie modifiée", {
          description: `La catégorie "${data.nom}" a été mise à jour`,
        });
      }
      handleCloseForm();
    } catch (error: any) {
      const errorMessage = error?.response?.data?.message || error?.message;
      toast.error(
        formMode === "create"
          ? "Erreur lors de la création"
          : "Erreur lors de la modification",
        { description: errorMessage },
      );
    }
  };

  // Supprimer une catégorie
  const handleDeleteCategory = async () => {
    if (!categoryToDelete) return;

    try {
      await deleteCategorie(categoryToDelete.uuid);
      toast.success("Catégorie supprimée", {
        description: `La catégorie "${categoryToDelete.nom}" a été supprimée`,
      });
      setCategoryToDelete(null);
    } catch (error: any) {
      const errorMessage = error?.response?.data?.message || error?.message;
      toast.error("Erreur lors de la suppression", {
        description: errorMessage,
      });
    }
  };

  const formTitle =
    formMode === "create"
      ? tAdmin("categories.createTitle")
      : "Modifier la catégorie";

  const formDescription =
    formMode === "create"
      ? tAdmin("categories.createDescription")
      : "Modifiez les informations de la catégorie.";

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-heading font-bold flex items-center gap-2">
            <FolderTree className="h-8 w-8" />
            {tAdmin("categories.title")}
          </h1>
          <p className="text-muted-foreground mt-1">
            {tAdmin("categories.description")}
          </p>
        </div>

        <Button
          leftIcon={<Plus className="h-4 w-4" />}
          onClick={handleOpenCreate}
        >
          {tAdmin("categories.newCategory")}
        </Button>
      </div>

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Total catégories</CardDescription>
            <CardTitle className="text-2xl">
              {categories?.length || 0}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Actives</CardDescription>
            <CardTitle className="text-2xl text-green-600">
              {categories?.length || 0}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Total deals</CardDescription>
            <CardTitle className="text-2xl">0</CardTitle>
          </CardHeader>
        </Card>
      </div>

      {/* Categories List */}
      <Card>
        <CardHeader>
          <CardTitle>Liste des catégories</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoadingCategories ? (
            <div className="text-center py-8 text-muted-foreground">
              Chargement...
            </div>
          ) : !categories || categories.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              Aucune catégorie pour le moment
            </div>
          ) : (
            <div className="space-y-2">
              {categories.map((category) => {
                const Icon = category.icone;
                return (
                  <div
                    key={category.uuid}
                    className="flex items-center gap-4 p-4 border rounded-lg bg-card hover:bg-muted/50 transition-colors"
                  >
                    {Icon && (
                      <span className="text-2xl">
                        <Icon />
                      </span>
                    )}

                    <div className="flex-1">
                      <div className="flex items-center gap-2">
                        <span className="font-medium">{category.nom}</span>
                        {category.description && (
                          <span className="text-sm text-muted-foreground">
                            {category.description}
                          </span>
                        )}
                      </div>
                    </div>

                    <div className="text-right">
                      <span className="font-medium">0</span>
                      <span className="text-sm text-muted-foreground ml-1">
                        deals
                      </span>
                    </div>

                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon">
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem
                          onClick={() => handleOpenEdit(category)}
                        >
                          <Pencil className="mr-2 h-4 w-4" />
                          {tAdmin("categories.edit")}
                        </DropdownMenuItem>
                        <DropdownMenuItem
                          className="text-destructive"
                          onClick={() => setCategoryToDelete(category)}
                        >
                          <Trash2 className="mr-2 h-4 w-4" />
                          {tAdmin("categories.delete")}
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Dialog Formulaire Unique (Création / Modification) */}
      <Dialog
        open={isFormOpen}
        onOpenChange={(open) => !open && handleCloseForm()}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{formTitle}</DialogTitle>
            <DialogDescription>{formDescription}</DialogDescription>
          </DialogHeader>

          <Form<CreateCategoryDTO | UpdateCategoryDTO>
            schema={categorySchema}
            defaultValues={{
              nom: selectedCategory?.nom || "",
              description: selectedCategory?.description || "",
              icone: selectedCategory?.icone || "",
            }}
            onSubmit={async ({ data }) => {
              await handleSubmitForm(data);
            }}
            resetOnDefaultValuesChange={true}
            submitLabel={formMode === "create" ? "Créer" : "Enregistrer"}
            resetLabel="Annuler"
            isLoading={isLoading}
            submitBtnProps={{
              leftIcon:
                formMode === "create" ? (
                  <Plus className="h-4 w-4" />
                ) : (
                  <Pencil className="h-4 w-4" />
                ),
            }}
            resetBtnProps={{
              leftIcon: <X className="h-4 w-4" />,
              onClick: handleCloseForm,
            }}
            columns={1}
            fields={[
              {
                name: "nom",
                label: "Nom de la catégorie",
                type: "text",
                placeholder: "Ex: Restaurants",
              },
              {
                name: "description",
                label: "Description",
                type: "text",
                placeholder: "Description de la catégorie",
              },
              {
                name: "icone",
                label: "Icône",
                type: "text",
                placeholder: "Ex: utensils",
              },
            ]}
          />
        </DialogContent>
      </Dialog>

      {/* AlertDialog Suppression */}
      <AlertDialog
        open={!!categoryToDelete}
        onOpenChange={(open) => !open && setCategoryToDelete(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Supprimer la catégorie</AlertDialogTitle>
            <AlertDialogDescription>
              Cette action est irréversible. Voulez-vous vraiment supprimer la
              catégorie{" "}
              <span className="font-semibold">{categoryToDelete?.nom}</span> ?
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isDeleting}>Annuler</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeleteCategory}
              disabled={isDeleting}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {isDeleting ? "Suppression..." : "Supprimer"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
