import { Checkbox } from "@/components/Checkbox";
import { Pagination } from "@/components/Pagination";
import { Radio } from "@/components/Radio";
import { Select } from "@/components/Select";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import VStack from "@/components/VStack";
import { useI18n } from "@/context/I18nContext";
import { Pen } from "lucide-react";
import React from "react";

export default function Home() {
  const { t } = useI18n();
  const [page, setPage] = React.useState(1);
  return (
    <VStack className="mx-auto">
      {t("hello")}
      <Button title="TE" tooltip="TT" leftIcon={<Pen />} />

      <Select
        groups={[
          {
            label: "test",
            items: [
              {
                label: "1",
                value: "1",
              },
              {
                label: "1",
                value: "2",
              },
            ],
          },
        ]}
      />
      <Radio label="tes ewr tw ywy t" position="left" name="r" />
      <Radio label="tes ewr tw ywy t" position="right" name="r" error="r" />

      <Checkbox label="tes ewr tw ywy t" position="left" name="r" />
      <Checkbox label="tes ewr tw ywy t" position="right" name="r" error="r" />
      <Input debounce={5000} onChange={(e) => console.log(e.target.value)} />

      <Pagination
        page={page}
        totalPages={42}
        onChange={setPage}
        totalItems={1000}
      />
    </VStack>
  );
}
