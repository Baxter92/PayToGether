import { useForm } from "react-hook-form";
import type * as z from "zod";
import Select, { type ISelectProps } from "@components/Select";
import { zodResolver } from "@hookform/resolvers/zod";
import RadioGroup, { type IRadioGroupProps } from "@components/RadioGroup";
import CheckboxGroup, {
  type ICheckboxGroupProps,
} from "@components/CheckboxGroup";
import { Input, type InputProps } from "@components/ui/input";
import Checkbox, { type ICheckboxProps } from "@components/Checkbox";
import Textarea from "@components/Textarea";

// Types pour la configuration du formulaire
export type FieldType =
  | "text"
  | "email"
  | "number"
  | "textarea"
  | "select"
  | "radio"
  | "checkbox"
  | "password";

export type IFieldConfig = { name: string; colSpan?: number; label: string } & (
  | ({
      type: "text";
    } & InputProps)
  | ({ type: "email" } & InputProps)
  | ({ type: "number" } & InputProps)
  | ({ type: "textarea" } & InputProps)
  | ({ type: "select" } & ISelectProps)
  | ({ type: "radio" } & IRadioGroupProps)
  | ({ type: "checkbox" } & (ICheckboxGroupProps | ICheckboxProps))
  | ({ type: "password" } & InputProps)
);

export interface IFieldGroup {
  title?: string;
  description?: string;
  columns?: number; // Nombre de colonnes par ligne (1-4)
  fields: IFieldConfig[];
}

export interface IFormContainerConfig {
  groups?: IFieldGroup[];
  fields?: IFieldConfig[]; // Option pour passer directement les champs
  columns?: number; // Nombre de colonnes si on utilise fields directement
  schema: z.ZodSchema<any>; // Schéma Zod pour la validation
  onSubmit: (data: any) => void;
  submitLabel?: string;
  resetLabel?: string;
}

const Form: React.FC<IFormContainerConfig> = ({
  groups,
  fields,
  columns = 1,
  schema,
  onSubmit,
  submitLabel = "Soumettre",
  resetLabel = "Réinitialiser",
}) => {
  // Normaliser la config : si fields est fourni, créer un groupe automatiquement
  const normalizedGroups: IFieldGroup[] =
    groups || (fields ? [{ fields, columns }] : []);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm({
    resolver: zodResolver(schema),
  });

  const getColClass = (cols: number = 1) => {
    const colMap: Record<number, string> = {
      1: "grid-cols-1",
      2: "grid-cols-1 md:grid-cols-2",
      3: "grid-cols-1 md:grid-cols-3",
      4: "grid-cols-1 md:grid-cols-2 lg:grid-cols-4",
    };
    return colMap[cols] || "grid-cols-1";
  };

  const getSpanClass = (span: number = 1) => {
    const spanMap: Record<number, string> = {
      1: "col-span-1",
      2: "col-span-2",
      3: "col-span-3",
      4: "col-span-4",
      5: "col-span-5",
      6: "col-span-6",
      7: "col-span-7",
      8: "col-span-8",
      9: "col-span-9",
      10: "col-span-10",
      11: "col-span-11",
      12: "col-span-12",
    };

    if (span >= 12) return "col-span-12";
    return spanMap[span] ?? "col-span-1";
  };

  const renderField = (field: IFieldConfig) => {
    const error = errors[field.name];
    const errorMessage = error?.message as string;

    const baseInputClass = `w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
      error ? "border-red-500" : "border-gray-300"
    } ${field.disabled ? "bg-gray-100 cursor-not-allowed" : ""}`;

    switch (field.type) {
      case "textarea":
        return (
          <Textarea
            {...register(field.name)}
            placeholder={field?.placeholder}
            disabled={field.disabled}
            className={`${baseInputClass} min-h-[100px]`}
            error={errorMessage}
          />
        );

      case "select": {
        const { onChange, ...rest } = register(field.name);
        return (
          <Select
            {...rest}
            disabled={field.disabled}
            triggerClassName={baseInputClass}
            items={field.items}
            {...field}
            onValueChange={(value) =>
              onChange({ target: { value, name: field.name } })
            }
            error={errorMessage}
          />
        );
      }

      case "radio": {
        const { onChange, ...rest } = register(field.name);
        return (
          <RadioGroup
            {...rest}
            disabled={field.disabled}
            items={field.items}
            onChange={(value) =>
              onChange({ target: { value, name: field.name } })
            }
            error={errorMessage}
          />
        );
      }

      case "checkbox":
        if ((field as ICheckboxGroupProps).items) {
          const { onChange, ...rest } = register(field.name);
          return (
            <CheckboxGroup
              {...rest}
              disabled={field.disabled}
              items={(field as ICheckboxGroupProps).items}
              onChange={(value) =>
                onChange({ target: { value, name: field.name } })
              }
              error={errorMessage}
            />
          );
        } else {
          // Single checkbox
          return (
            <Checkbox
              {...register(field.name)}
              disabled={field.disabled}
              label={(field as ICheckboxProps).label}
              error={errorMessage}
            />
          );
        }

      default:
        return (
          <Input
            type={field.type}
            {...register(field.name, {
              valueAsNumber: field.type === "number",
            })}
            {...field}
            className={baseInputClass}
            error={errorMessage}
          />
        );
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-8">
      {normalizedGroups.map((group, groupIdx) => (
        <div
          key={groupIdx}
          className="bg-white p-6 rounded-lg shadow-sm border border-gray-200"
        >
          {group.title && (
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              {group.title}
            </h3>
          )}
          {group.description && (
            <p className="text-sm text-gray-600 mb-4">{group.description}</p>
          )}

          <div className={`grid ${getColClass(group.columns)} gap-4`}>
            {group.fields.map((field, fieldIdx) => (
              <div
                key={fieldIdx}
                className={field.colSpan ? getSpanClass(field.colSpan) : ""}
              >
                {renderField(field)}
              </div>
            ))}
          </div>
        </div>
      ))}

      <div className="flex gap-3">
        <button
          type="submit"
          className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
        >
          {submitLabel}
        </button>
        <button
          type="button"
          onClick={() => reset()}
          className="px-6 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 transition-colors"
        >
          {resetLabel}
        </button>
      </div>
    </form>
  );
};

export default Form;
