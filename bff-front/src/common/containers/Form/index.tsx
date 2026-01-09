import {
  useForm,
  Controller,
  type UseFormReturn,
  type FieldValues,
} from "react-hook-form";
import type * as z from "zod";
import { zodResolver } from "@hookform/resolvers/zod";

import Select, { type ISelectProps } from "@components/Select";
import RadioGroup, { type IRadioGroupProps } from "@components/RadioGroup";
import CheckboxGroup, {
  type ICheckboxGroupProps,
} from "@components/CheckboxGroup";
import Checkbox, { type ICheckboxProps } from "@components/Checkbox";
import Textarea from "@components/Textarea";
import { Input, type InputProps } from "@components/ui/input";
import {
  DateInput,
  DateTimeInput,
  TimeInput,
  HStack,
} from "@/common/components";
import { Button, type IButtonProps } from "@/common/components/ui/button";
import type { IDateInputProps } from "@/common/components/DateInput";
import type { ITimeInputProps } from "@/common/components/TimeInput";
import type { IDateTimeInputProps } from "@/common/components/DateTimeInput";
import { cn } from "@/common/lib/utils";

/* ==============================
   Types
============================== */

export type FieldType =
  | "text"
  | "email"
  | "number"
  | "password"
  | "textarea"
  | "select"
  | "radio"
  | "checkbox"
  | "date"
  | "time"
  | "datetime";

export type IFieldConfig = {
  name: string;
  label: string;
  colSpan?: number;
  disabled?: boolean;
  render?: (field: IFieldConfig, form: UseFormReturn<any>) => React.ReactNode;
} & (
  | ({ type: "text" | "email" | "number" | "password" } & InputProps)
  | ({ type: "textarea" } & InputProps)
  | ({ type: "select" } & ISelectProps)
  | ({ type: "radio" } & IRadioGroupProps)
  | ({ type: "checkbox" } & (ICheckboxProps | ICheckboxGroupProps))
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

export type IFormContainerConfig<T = FieldValues> = {
  form?: UseFormReturn<any>;
  groups?: IFieldGroup[];
  fields?: IFieldConfig[];
  columns?: number;
  schema?: z.ZodSchema<any>;
  defaultValues?: Partial<T>;
  readOnly?: boolean; // ✅ NOUVELLE PROP
  onSubmit?: (options: IFormSubmitOptions<T>) => void | Promise<void>;
  submitLabel?: string;
  resetLabel?: string;
  showSubmitButton?: boolean;
  showResetButton?: boolean;
  submitBtnProps?: IButtonProps;
  resetBtnProps?: IButtonProps;
};

/* ==============================
   Component
============================== */

const Form = <T extends FieldValues>({
  form: externalForm,
  groups,
  fields,
  columns = 1,
  schema,
  defaultValues,
  readOnly = false,
  onSubmit,
  submitLabel = "Soumettre",
  resetLabel = "Réinitialiser",
  showSubmitButton = true,
  showResetButton = true,
  submitBtnProps,
  resetBtnProps,
  ...props
}: IFormContainerConfig<T> &
  Omit<React.ComponentProps<"form">, "onSubmit">) => {
  const internalForm = useForm<any>({
    resolver: schema ? zodResolver(schema as any) : undefined,
    defaultValues,
  });

  const form = externalForm ?? internalForm;

  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = form;

  const normalizedGroups: IFieldGroup[] =
    groups || (fields ? [{ fields, columns }] : []);

  const getColClass = (cols = 1) =>
    ({
      1: "grid-cols-1",
      2: "grid-cols-1 md:grid-cols-2",
      3: "grid-cols-1 md:grid-cols-3",
      4: "grid-cols-1 md:grid-cols-2 lg:grid-cols-4",
    }[cols] || "grid-cols-1");

  const renderField = (field: IFieldConfig) => {
    if (field.render) return field.render(field, form);

    const error = errors[field.name]?.message as string | undefined;
    const isDisabled = readOnly || field.disabled;

    /* ===== INPUTS NATIFS ===== */
    if (
      field.type === "text" ||
      field.type === "email" ||
      field.type === "number" ||
      field.type === "password"
    ) {
      return (
        <Input
          {...register(field.name, {
            valueAsNumber: field.type === "number",
          })}
          {...field}
          disabled={isDisabled}
          error={error}
        />
      );
    }

    /* ===== TEXTAREA ===== */
    if (field.type === "textarea") {
      return (
        <Textarea
          {...register(field.name)}
          {...field}
          disabled={isDisabled}
          error={error}
        />
      );
    }

    /* ===== SELECT (FULL WIDTH FIX) ===== */
    if (field.type === "select") {
      return (
        <Controller
          name={field.name}
          control={control}
          render={({ field: f }) => (
            <Select
              {...field}
              value={f.value}
              onValueChange={f.onChange}
              disabled={isDisabled}
              className="w-full"
              triggerClassName="w-full"
              error={error}
            />
          )}
        />
      );
    }

    /* ===== RADIO ===== */
    if (field.type === "radio") {
      return (
        <Controller
          name={field.name}
          control={control}
          render={({ field: f }) => (
            <RadioGroup
              {...field}
              value={f.value}
              onChange={f.onChange}
              disabled={isDisabled}
              error={error}
            />
          )}
        />
      );
    }

    /* ===== CHECKBOX ===== */
    if (field.type === "checkbox") {
      if ("items" in field) {
        return (
          <Controller
            name={field.name}
            control={control}
            render={({ field: f }) => (
              <CheckboxGroup
                {...field}
                value={f.value}
                onChange={f.onChange}
                disabled={isDisabled}
                error={error}
              />
            )}
          />
        );
      }

      return (
        <Controller
          name={field.name}
          control={control}
          render={({ field: f }) => (
            <Checkbox
              {...field}
              checked={!!f.value}
              onCheckedChange={f.onChange}
              disabled={isDisabled}
              error={error}
            />
          )}
        />
      );
    }

    /* ===== DATE / TIME ===== */
    if (field.type === "date") {
      return (
        <Controller
          name={field.name}
          control={control}
          render={({ field: f }) => (
            <DateInput
              {...field}
              value={f.value}
              onChange={f.onChange}
              disabled={isDisabled}
              error={error}
            />
          )}
        />
      );
    }

    if (field.type === "time") {
      return (
        <Controller
          name={field.name}
          control={control}
          render={({ field: f }) => (
            <TimeInput
              {...field}
              value={f.value}
              onChange={f.onChange}
              disabled={isDisabled}
              error={error}
            />
          )}
        />
      );
    }

    if (field.type === "datetime") {
      return (
        <Controller
          name={field.name}
          control={control}
          render={({ field: f }) => (
            <DateTimeInput
              {...field}
              value={f.value}
              onChange={f.onChange}
              disabled={isDisabled}
              error={error}
            />
          )}
        />
      );
    }

    return null;
  };

  return (
    <form
      onSubmit={handleSubmit((data) => onSubmit({ data, form }))}
      {...props}
    >
      {normalizedGroups.map((group, idx) => (
        <div key={idx} className={cn("mb-6", group.className)}>
          {group.title && (
            <h3 className="text-lg font-semibold mb-2">{group.title}</h3>
          )}
          {group.description && (
            <p className="text-sm text-muted mb-4">{group.description}</p>
          )}

          <div className={cn("grid gap-4", getColClass(group.columns))}>
            {group.fields.map((field) => (
              <div key={field.name}>{renderField(field)}</div>
            ))}
          </div>
        </div>
      ))}

      {!readOnly && (
        <HStack spacing={4}>
          {showSubmitButton && (
            <Button type="submit" loading={isSubmitting} {...submitBtnProps}>
              {submitLabel}
            </Button>
          )}
          {showResetButton && (
            <Button
              type="button"
              variant="secondary"
              onClick={() => reset(defaultValues as any)}
              {...resetBtnProps}
            >
              {resetLabel}
            </Button>
          )}
        </HStack>
      )}
    </form>
  );
};

export default Form;
