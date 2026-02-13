import { z } from "zod";

export const categorySchema = z.object({
  name: z.string().min(1, "Nom requis").max(50, "Nom trop long"),
  slug: z.string().optional(),
  icone: z.string().optional(),
  description: z.string().optional(),
});
