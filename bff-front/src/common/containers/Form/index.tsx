import { useForm, type Path, type UseFormReturn } from "react-hook-form";
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
import { cn } from "@/common/lib/utils";
import {
  DateInput,
  DateTimeInput,
  HStack,
  TimeInput,
} from "@/common/components";
import { Button, type IButtonProps } from "@/common/components/ui/button";
import type { IDateInputProps } from "@/common/components/DateInput";
import type { ITimeInputProps } from "@/common/components/TimeInput";
import type { IDateTimeInputProps } from "@/common/components/DateTimeInput";

// Types pour la configuration du formulaire
export type FieldType =
  | "text"
  | "email"
  | "number"
  | "textarea"
  | "select"
  | "radio"
  | "checkbox"
  | "password"
  | "date"
  | "time"
  | "datetime";

export type IFieldConfig = {
  name: string;
  colSpan?: number;
  label: string;
  render?: (field: IFieldConfig, form: UseFormReturn) => React.ReactNode;
} & (
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
  | ({ type: "date" } & IDateInputProps)
  | ({ type: "time" } & ITimeInputProps)
  | ({ type: "datetime" } & IDateTimeInputProps)
);

export interface IFieldGroup {
  title?: string;
  description?: string;
  columns?: number; // Nombre de colonnes par ligne (1-4)
  fields: IFieldConfig[];
  className?: string;
}

export interface IFormSubmitOptions<T = any> {
  data: T;
  form: UseFormReturn;
}

export interface IFormResetOptions<T = any> {
  form: UseFormReturn;
}

export type IFormContainerConfig<T = any> = {
  form?: UseFormReturn<T>; // <--- was UseFormReturn (non-generic)
  groups?: IFieldGroup[];
  fields?: IFieldConfig[];
  columns?: number;
  schema: z.ZodSchema<T>;
  defaultValues?: Partial<T>;
  onSubmit: (options: IFormSubmitOptions<T>) => void | Promise<void>;
  onReset?: (options: IFormResetOptions<T>) => void | Promise<void>;
  submitLabel?: string;
  resetLabel?: string;
  showResetButton?: boolean;
  titleClassName?: string;
  descriptionClassName?: string;
  submitBtnProps?: IButtonProps;
  resetBtnProps?: IButtonProps;
};

const Form = <T extends Record<string, any>>({
  form: externalForm,
  groups,
  fields,
  columns = 1,
  schema,
  defaultValues,
  onSubmit,
  onReset,
  submitLabel = "Soumettre",
  resetLabel = "Réinitialiser",
  showResetButton = true,
  titleClassName,
  descriptionClassName,
  submitBtnProps,
  resetBtnProps,
  ...props
}: IFormContainerConfig<T> &
  Omit<React.ComponentProps<"form">, "onSubmit">) => {
  // Normaliser la config : si fields est fourni, créer un groupe automatiquement
  const normalizedGroups: IFieldGroup[] =
    groups || (fields ? [{ fields, columns }] : []);

  const internalForm = useForm<T>({
    resolver: zodResolver(schema as any),
    defaultValues: defaultValues as any,
  });

  // Choisir la source de vérité : si externalForm est fourni on l'utilise, sinon internalForm
  const form = externalForm ?? internalForm;

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = form;

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
    // Si une fonction render personnalisée est fournie, l'utiliser
    if (field.render) {
      return field.render(field, form);
    }

    const error = errors[field.name];
    const errorMessage = error?.message as string;

    const baseInputClass = `w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
      error ? "border-red-500" : "border-gray-300"
    } ${(field as any)?.disabled ? "bg-gray-100 cursor-not-allowed" : ""}`;

    switch (field.type) {
      case "textarea":
        return (
          <Textarea
            {...register(field.name as Path<T>)}
            {...field}
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

      case "date": {
        const { onChange, ...rest } = register(field.name);
        return (
          <DateInput
            {...rest}
            {...field}
            max={field.max}
            min={field.min}
            onChange={(date) =>
              onChange({ target: { value: date, name: field.name } })
            }
            error={errorMessage}
          />
        );
      }

      case "datetime": {
        const { onChange, ...rest } = register(field.name);
        return (
          <DateTimeInput
            {...rest}
            {...field}
            onChange={(date) =>
              onChange({ target: { value: date, name: field.name } })
            }
            error={errorMessage}
          />
        );
      }

      case "time": {
        const { onChange, ...rest } = register(field.name);
        return (
          <TimeInput
            {...rest}
            {...field}
            onChange={(time) =>
              onChange({ target: { value: time, name: field.name } })
            }
            error={errorMessage}
          />
        );
      }
      default:
        return (
          <Input
            {...register(field.name as Path<T>, {
              valueAsNumber: field.type === "number",
            })}
            {...field}
            className={baseInputClass}
            error={errorMessage}
          />
        );
    }
  };

  const handleFormSubmit = async (data: T) => {
    await onSubmit({ data, form });
  };

  const handleFormReset = async () => {
    if (onReset) {
      await onReset({ form });
    } else {
      // Comportement par défaut : réinitialiser aux valeurs par défaut
      reset(defaultValues as any);
    }
  };

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} {...props}>
      {normalizedGroups.map((group, groupIdx) => (
        <div
          key={groupIdx}
          className={cn(group.className, groupIdx > 0 && "mt-6")}
        >
          {group.title && (
            <h3
              className={cn(
                "text-lg font-semibold text-gray-900 mb-2",
                titleClassName
              )}
            >
              {group.title}
            </h3>
          )}
          {group.description && (
            <p
              className={cn("text-sm text-gray-600 mb-4", descriptionClassName)}
            >
              {group.description}
            </p>
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

      <HStack spacing={5} className="mt-6" wrap>
        <Button
          type="submit"
          disabled={isSubmitting}
          loading={isSubmitting}
          {...submitBtnProps}
        >
          {isSubmitting ? "En cours..." : submitLabel}
        </Button>
        {showResetButton && (
          <Button
            type="button"
            variant="secondary"
            disabled={isSubmitting}
            {...resetBtnProps}
            onClick={handleFormReset}
          >
            {resetLabel}
          </Button>
        )}
      </HStack>
    </form>
  );
};

export default Form;
