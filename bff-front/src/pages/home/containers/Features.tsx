import { Heading } from "@/common/containers/Heading";
import Grid from "@components/Grid";
import { features } from "@constants/data";

const Features = () => {
  return (
    <section className="py-24 bg-gradient-to-b from-background via-muted/30 to-background relative overflow-hidden">
      {/* Decorative elements */}
      <div className="absolute top-0 left-1/4 w-96 h-96 bg-primary/5 rounded-full blur-3xl" />
      <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-accent/5 rounded-full blur-3xl" />

      <div className="max-w-7xl mx-auto px-4 relative z-10">
        <Heading
          title="Comment ça marche ?"
          align="center"
          level={1}
          spacing={16}
          description="DealToGether révolutionne le shopping en groupe. Voici ce qui rend notre plateforme unique."
          className="mb-16"
        />

        <Grid cols={{ md: 2, lg: 4 }} gap="gap-8">
          {features.map((feature, index) => {
            const Icon = feature.icon;
            return (
              <div
                key={index}
                className="group text-center p-6 rounded-2xl bg-card/50 backdrop-blur-sm border border-border/50 hover:border-primary/30 hover:bg-card hover:shadow-[var(--shadow-card-hover)] transition-all duration-500 hover:-translate-y-2"
                style={{ animationDelay: `${index * 100}ms` }}
              >
                <div className="mb-5 flex justify-center">
                  <div className="p-4 bg-gradient-to-br from-primary/10 to-primary/5 rounded-2xl group-hover:from-primary/20 group-hover:to-primary/10 group-hover:scale-110 transition-all duration-300 shadow-inner">
                    <Icon className="w-7 h-7 text-primary group-hover:text-primary-600 transition-colors duration-300" />
                  </div>
                </div>
                <h3 className="text-lg font-bold font-[family-name:var(--font-heading)] text-foreground mb-3 group-hover:text-primary transition-colors duration-300">
                  {feature.title}
                </h3>
                <p className="text-muted-foreground text-sm leading-relaxed">
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
