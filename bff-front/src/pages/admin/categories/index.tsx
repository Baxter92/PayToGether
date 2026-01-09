import type { ReactElement } from "react";
import { useState } from "react";
import {
  FolderTree,
  Plus,
  MoreHorizontal,
  Pencil,
  Trash2,
  X,
} from "lucide-react";
import { Button } from "@/common/components/ui/button";
import { Badge } from "@/common/components/ui/badge";
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
  DialogTrigger,
} from "@/common/components/ui/dialog";
import Form from "@/common/containers/Form";
import { z } from "zod";
import { categories } from "@/common/constants/data";
import { toast } from "sonner";

const categorySchema = z.object({
  name: z.string().min(1, "Nom requis").max(50, "Nom trop long"),
  slug: z.string().min(1, "Slug requis").max(50, "Slug trop long"),
});

type CategoryFormData = z.infer<typeof categorySchema>;
type Category = {
  id: string;
  name: string;
  icon: any;
  slug: string;
  count: number;
  isActive: boolean;
};

export default function AdminCategories(): ReactElement {
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editCategory, setEditCategory] = useState<Category | null>(null);
  const [deleteCategory, setDeleteCategory] = useState<Category | null>(null);

  const handleCreateCategory = async ({ data }: { data: CategoryFormData }) => {
    console.log("New category:", data);
    toast.success("Catégorie créée", {
      description: `La catégorie "${data.name}" a été créée`,
    });
    setIsDialogOpen(false);
  };

  const handleUpdateCategory = async ({ data }: { data: CategoryFormData }) => {
    if (!editCategory) return;

    toast.success("Catégorie modifiée", {
      description: `La catégorie "${data.name}" a été mise à jour`,
    });

    setEditCategory(null);
  };

  const handleDeleteCategory = async () => {
    if (!deleteCategory) return;

    toast.success("Catégorie supprimée", {
      description: `La catégorie "${deleteCategory.name}" a été supprimée`,
    });

    setDeleteCategory(null);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-heading font-bold flex items-center gap-2">
            <FolderTree className="h-8 w-8" />
            Catégories
          </h1>
          <p className="text-muted-foreground mt-1">
            Gérez les catégories de deals
          </p>
        </div>

        <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
          <DialogTrigger asChild>
            <Button leftIcon={<Plus className="h-4 w-4" />}>
              Nouvelle catégorie
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Créer une catégorie</DialogTitle>
              <DialogDescription>
                Ajoutez une nouvelle catégorie pour organiser vos deals.
              </DialogDescription>
            </DialogHeader>
            <Form<CategoryFormData>
              schema={categorySchema}
              defaultValues={{ name: "", slug: "" }}
              onSubmit={handleCreateCategory}
              submitLabel="Créer"
              resetLabel="Annuler"
              submitBtnProps={{
                leftIcon: <Plus className="h-4 w-4" />,
              }}
              resetBtnProps={{
                leftIcon: <X className="h-4 w-4" />,
                onClick: () => setIsDialogOpen(false),
              }}
              columns={1}
              fields={[
                {
                  name: "name",
                  label: "Nom de la catégorie",
                  type: "text",
                  placeholder: "Ex: Restaurants",
                },
                {
                  name: "icon",
                  label: "Icône",
                  type: "text",
                },
              ]}
            />
          </DialogContent>
        </Dialog>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Total catégories</CardDescription>
            <CardTitle className="text-2xl">{categories.length}</CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Actives</CardDescription>
            <CardTitle className="text-2xl text-green-600">
              {categories.filter((c) => c.isActive).length}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Total deals</CardDescription>
            <CardTitle className="text-2xl">
              {categories.reduce((acc, c) => acc + c.count, 0)}
            </CardTitle>
          </CardHeader>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Liste des catégories</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            {categories.map((category) => {
              const Icon = category.icon;
              return (
                <div
                  key={category.id}
                  className="flex items-center gap-4 p-4 border rounded-lg bg-card hover:bg-muted/50 transition-colors"
                >
                  <span className="text-2xl">
                    <Icon />
                  </span>

                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <span className="font-medium">{category.name}</span>
                      {!category.isActive && (
                        <Badge colorScheme="secondary">Inactive</Badge>
                      )}
                    </div>
                    <span className="text-sm text-muted-foreground">
                      /{category.slug}
                    </span>
                  </div>

                  <div className="text-right">
                    <span className="font-medium">{category.count}</span>
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
                        onClick={() => setEditCategory(category)}
                      >
                        <Pencil className="mr-2 h-4 w-4" />
                        Modifier
                      </DropdownMenuItem>
                      <DropdownMenuItem
                        className="text-destructive"
                        onClick={() => setDeleteCategory(category)}
                      >
                        <Trash2 className="mr-2 h-4 w-4" />
                        Supprimer
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
              );
            })}
          </div>
        </CardContent>
      </Card>
      <Dialog open={!!editCategory} onOpenChange={() => setEditCategory(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Modifier la catégorie</DialogTitle>
            <DialogDescription>
              Modifiez les informations de la catégorie.
            </DialogDescription>
          </DialogHeader>

          {editCategory && (
            <Form<CategoryFormData>
              schema={categorySchema}
              defaultValues={{
                name: editCategory.name,
                slug: editCategory.slug,
              }}
              onSubmit={handleUpdateCategory}
              submitLabel="Enregistrer"
              resetLabel="Annuler"
              submitBtnProps={{
                leftIcon: <Pencil className="h-4 w-4" />,
              }}
              resetBtnProps={{
                leftIcon: <X className="h-4 w-4" />,
                onClick: () => setEditCategory(null),
              }}
              columns={1}
              fields={[
                {
                  name: "name",
                  label: "Nom de la catégorie",
                  type: "text",
                },
                {
                  name: "slug",
                  label: "Slug",
                  type: "text",
                },
              ]}
            />
          )}
        </DialogContent>
      </Dialog>

      <Dialog
        open={!!deleteCategory}
        onOpenChange={() => setDeleteCategory(null)}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Supprimer la catégorie</DialogTitle>
            <DialogDescription>
              Cette action est irréversible. Voulez-vous vraiment supprimer la
              catégorie{" "}
              <span className="font-semibold">{deleteCategory?.name}</span>?
            </DialogDescription>
          </DialogHeader>

          <div className="flex justify-end gap-2 mt-6">
            <Button
              colorScheme="secondary"
              onClick={() => setDeleteCategory(null)}
              leftIcon={<X className="h-4 w-4" />}
            >
              Annuler
            </Button>
            <Button
              colorScheme="danger"
              onClick={handleDeleteCategory}
              leftIcon={<Trash2 className="h-4 w-4" />}
            >
              Supprimer
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
