import Grid from "@/components/Grid";
import { features } from "@/constants/data";

const Features = () => {
  return (
    <section className="py-20 bg-muted/30">
      <div className="max-w-7xl mx-auto px-4">
        <div className="text-center mb-16">
          <h2 className="text-3xl md:text-4xl font-bold text-foreground mb-4">
            Comment ça marche ?
          </h2>
          <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
            PayToGether révolutionne le shopping en groupe. Voici ce qui rend
            notre plateforme unique.
          </p>
        </div>

        <Grid cols={{ md: 2, lg: 4 }} gap="gap-8">
          {features.map((feature, index) => {
            const Icon = feature.icon;
            return (
              <div key={index} className="text-center">
                <div className="mb-4 flex justify-center">
                  <div className="p-3 bg-primary-500/10 rounded-lg">
                    <Icon className="w-6 h-6 text-primary-500" />
                  </div>
                </div>
                <h3 className="text-lg font-semibold text-foreground mb-2">
                  {feature.title}
                </h3>
                <p className="text-muted-foreground text-sm">
                  {feature.description}
                </p>
              </div>
            );
          })}
        </Grid>
      </div>
    </section>
  );
};

export default Features;
