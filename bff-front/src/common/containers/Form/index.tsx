import { useForm, type UseFormReturn, type FieldValues } from "react-hook-form";
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
  render?: (field: IFieldConfig, form: UseFormReturn<any>) => React.ReactNode;
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
  columns?: number;
  fields: IFieldConfig[];
  className?: string;
}

export interface IFormSubmitOptions<T = FieldValues> {
  data: T;
  form: UseFormReturn<any>;
}

export interface IFormResetOptions {
  form: UseFormReturn<any>;
}

export type IFormContainerConfig<T = FieldValues> = {
  form?: UseFormReturn<any>;
  groups?: IFieldGroup[];
  fields?: IFieldConfig[];
  columns?: number;
  schema?: z.ZodSchema<any>;
  defaultValues?: Partial<T>;
  onSubmit: (options: IFormSubmitOptions<T>) => void | Promise<void>;
  onReset?: (options: IFormResetOptions) => void | Promise<void>;
  submitLabel?: string;
  resetLabel?: string;
  showResetButton?: boolean;
  titleClassName?: string;
  descriptionClassName?: string;
  submitBtnProps?: IButtonProps;
  resetBtnProps?: IButtonProps;
};

const Form = <T extends FieldValues>({
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

  const internalForm = useForm<any>({
    ...(schema ? { resolver: zodResolver(schema as any) as any } : {}),
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
    } ${(field as InputProps)?.disabled ? "bg-gray-100 cursor-not-allowed" : ""}`;

    switch (field.type) {
      case "textarea": {
        const { label: _label, type: _type, colSpan: _colSpan, render: _render, name, ...textareaProps } = field as IFieldConfig & { type: "textarea" };
        return (
          <Textarea
            {...register(name)}
            placeholder={textareaProps?.placeholder}
            disabled={textareaProps.disabled}
            className={`${baseInputClass} min-h-[100px]`}
            error={errorMessage}
          />
        );
      }

      case "select": {
        const { onChange: _onChange, ...rest } = register(field.name);
        const selectField = field as IFieldConfig & ISelectProps;
        return (
          <Select
            {...rest}
            disabled={selectField.disabled}
            triggerClassName={baseInputClass}
            items={selectField.items}
            label={selectField.label}
            onValueChange={(value) =>
              form.setValue(field.name, value)
            }
            error={errorMessage}
          />
        );
      }

      case "radio": {
        const { onChange: _onChange, ...rest } = register(field.name);
        const radioField = field as IFieldConfig & IRadioGroupProps;
        return (
          <RadioGroup
            {...rest}
            disabled={radioField.disabled}
            items={radioField.items}
            onChange={(value) =>
              form.setValue(field.name, value)
            }
            error={errorMessage}
          />
        );
      }

      case "checkbox":
        if ((field as ICheckboxGroupProps).items) {
          const { onChange: _onChange, ...rest } = register(field.name);
          const checkboxGroupField = field as IFieldConfig & ICheckboxGroupProps;
          return (
            <CheckboxGroup
              {...rest}
              disabled={checkboxGroupField.disabled}
              items={checkboxGroupField.items}
              onChange={(value) =>
                form.setValue(field.name, value)
              }
              error={errorMessage}
            />
          );
        } else {
          // Single checkbox
          const checkboxField = field as IFieldConfig & ICheckboxProps;
          return (
            <Checkbox
              {...register(field.name)}
              disabled={checkboxField.disabled}
              label={checkboxField.label}
              error={errorMessage}
            />
          );
        }

      case "date": {
        const { onChange: _onChange, ...rest } = register(field.name);
        const dateField = field as IFieldConfig & IDateInputProps;
        return (
          <DateInput
            {...rest}
            label={dateField.label}
            max={dateField.max}
            min={dateField.min}
            onChange={(date) =>
              form.setValue(field.name, date)
            }
            error={errorMessage}
          />
        );
      }

      case "datetime": {
        const { onChange: _onChange, ...rest } = register(field.name);
        const datetimeField = field as IFieldConfig & IDateTimeInputProps;
        return (
          <DateTimeInput
            {...rest}
            label={datetimeField.label}
            onChange={(date) =>
              form.setValue(field.name, date)
            }
            error={errorMessage}
          />
        );
      }

      case "time": {
        const { onChange: _onChange, ...rest } = register(field.name);
        const timeField = field as IFieldConfig & ITimeInputProps;
        return (
          <TimeInput
            {...rest}
            label={timeField.label}
            onChange={(time) =>
              form.setValue(field.name, time)
            }
            error={errorMessage}
          />
        );
      }
      default: {
        const inputField = field as IFieldConfig & InputProps;
        return (
          <Input
            {...register(field.name, {
              valueAsNumber: field.type === "number",
            })}
            type={field.type}
            label={inputField.label}
            placeholder={inputField.placeholder}
            disabled={inputField.disabled}
            className={baseInputClass}
            error={errorMessage}
          />
        );
      }
    }
  };

  const handleFormSubmit = async (data: FieldValues) => {
    await onSubmit({ data: data as T, form });
  };

  const handleFormReset = async () => {
    if (onReset) {
      await onReset({ form });
    } else {
      // Comportement par défaut : réinitialiser aux valeurs par défaut
      reset(defaultValues as FieldValues);
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