import { useState, useEffect } from "react";
import { ChevronLeft, ChevronRight, Play, Sparkles } from "lucide-react";
import { Button } from "../ui/button";
import HStack from "../hstack";

// Types pour les slides
interface Slide {
  id: number;
  title: string;
  subtitle: string;
  description: string;
  buttonText: string;
  buttonLink: string;
  image: string;
  gradient: string;
  textColor: string;
  badge?: string;
}

const Hero = () => {
  const [currentSlide, setCurrentSlide] = useState(0);
  const [isAutoPlaying, setIsAutoPlaying] = useState(true);

  // Données des slides - FACILEMENT CUSTOMISABLE
  const slides: Slide[] = [
    {
      id: 1,
      title: "Découvrez les Meilleurs Deals",
      subtitle: "Jusqu'à 70% de réduction",
      description:
        "Restaurants, spa, activités et bien plus encore dans votre ville",
      buttonText: "Explorer maintenant",
      buttonLink: "/deals",
      image:
        "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=1200&q=80",
      gradient: "from-purple-600/50 to-pink-600/50",
      textColor: "text-white",
      badge: "Nouveauté",
    },
    {
      id: 2,
      title: "Bien-être & Détente",
      subtitle: "Offres exclusives Spa",
      description:
        "Profitez d'un moment de relaxation avec nos offres spa et massage",
      buttonText: "Voir les offres spa",
      buttonLink: "/deals/spa",
      image:
        "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=1200&q=80",
      gradient: "from-teal-600/50 to-cyan-600/50",
      textColor: "text-white",
      badge: "Populaire",
    },
    {
      id: 3,
      title: "Saveurs Gastronomiques",
      subtitle: "Restaurants étoilés à prix réduits",
      description:
        "Découvrez les meilleurs restaurants de votre ville à des prix imbattables",
      buttonText: "Réserver une table",
      buttonLink: "/deals/restaurants",
      image:
        "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=1200&q=80",
      gradient: "from-orange-600/50 to-red-600/50",
      textColor: "text-white",
      badge: "Tendance",
    },
    {
      id: 4,
      title: "Activités & Loisirs",
      subtitle: "Vivez des expériences uniques",
      description:
        "Sport, culture, aventure... Trouvez l'activité qui vous correspond",
      buttonText: "Découvrir les activités",
      buttonLink: "/deals/activities",
      image:
        "https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3?w=1200&q=80",
      gradient: "from-blue-600/50 to-indigo-600/50",
      textColor: "text-white",
      badge: "Aventure",
    },
  ];

  // Auto-play
  useEffect(() => {
    if (!isAutoPlaying) return;

    const interval = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % slides.length);
    }, 5000);

    return () => clearInterval(interval);
  }, [isAutoPlaying, slides.length]);

  const goToSlide = (index: number) => {
    setCurrentSlide(index);
    setIsAutoPlaying(false);
    setTimeout(() => setIsAutoPlaying(true), 10000);
  };

  const nextSlide = () => {
    setCurrentSlide((prev) => (prev + 1) % slides.length);
    setIsAutoPlaying(false);
    setTimeout(() => setIsAutoPlaying(true), 10000);
  };

  const prevSlide = () => {
    setCurrentSlide((prev) => (prev - 1 + slides.length) % slides.length);
    setIsAutoPlaying(false);
    setTimeout(() => setIsAutoPlaying(true), 10000);
  };

  return (
    <div className="relative w-full h-[400px] md:h-[400px] overflow-hidden bg-gray-900">
      {/* Slides Container */}
      <div
        className="flex transition-transform duration-700 ease-out h-full"
        style={{ transform: `translateX(-${currentSlide * 100}%)` }}
      >
        {slides.map((slide) => (
          <div key={slide.id} className="min-w-full h-full relative">
            {/* Background Image */}
            <div
              className="absolute inset-0 bg-cover bg-center"
              style={{ backgroundImage: `url(${slide.image})` }}
            />

            {/* Gradient Overlay */}
            <div
              className={`absolute inset-0 bg-linear-to-r ${slide.gradient}`}
            />

            {/* Content */}
            <div className="relative h-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
              <div className="flex items-center h-full">
                <div className="max-w-2xl space-y-6 animate-fade-in">
                  {/* Badge */}
                  {slide.badge && (
                    <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white/20 backdrop-blur-sm border border-white/30">
                      <Sparkles className="w-4 h-4" />
                      <span className="text-sm font-semibold">
                        {slide.badge}
                      </span>
                    </div>
                  )}

                  {/* Subtitle */}
                  <p
                    className={`text-md md:text-xl font-medium ${slide.textColor} uppercase tracking-wide`}
                  >
                    {slide.subtitle}
                  </p>

                  {/* Title */}
                  <h1
                    className={`text-2xl md:text-3xl font-bold ${slide.textColor} leading-tight`}
                  >
                    {slide.title}
                  </h1>

                  {/* Description */}
                  <p
                    className={`text-md md:text-lg ${slide.textColor} opacity-90 max-w-xl`}
                  >
                    {slide.description}
                  </p>

                  {/* Button */}
                  <Button
                    className="bg-white text-gray-900 rounded-full"
                    asChild
                  >
                    <a href={slide.buttonLink}>
                      {slide.buttonText}
                      <Play className=" fill-current" />
                    </a>
                  </Button>

                  {/* Stats or Features */}
                  <div className="flex gap-8 pt-3">
                    <div>
                      <p className={`text-md font-bold ${slide.textColor}`}>
                        5000+
                      </p>
                      <p className={`text-sm ${slide.textColor} opacity-80`}>
                        Offres disponibles
                      </p>
                    </div>
                    <div>
                      <p className={`text-md font-bold ${slide.textColor}`}>
                        98%
                      </p>
                      <p className={`text-sm ${slide.textColor} opacity-80`}>
                        Clients satisfaits
                      </p>
                    </div>
                    <div>
                      <p className={`text-md font-bold ${slide.textColor}`}>
                        24/7
                      </p>
                      <p className={`text-sm ${slide.textColor} opacity-80`}>
                        Support client
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Navigation Arrows */}
      <HStack className="absolute top-1 right-0">
        <button
          onClick={prevSlide}
          className="p-1 rounded-full bg-white/20 backdrop-blur-sm border border-white/30 text-white hover:bg-white/30 transition-all"
          aria-label="Slide précédent"
        >
          <ChevronLeft className="w-6 h-6 group-hover:scale-110 transition-transform" />
        </button>

        <button
          onClick={nextSlide}
          className="p-1 rounded-full bg-white/20 backdrop-blur-sm border border-white/30 text-white hover:bg-white/30 transition-all"
          aria-label="Slide suivant"
        >
          <ChevronRight className="w-6 h-6 group-hover:scale-110 transition-transform" />
        </button>
        <button
          onClick={() => setIsAutoPlaying(!isAutoPlaying)}
          className="p-1 ml-1 rounded-full bg-white/20 backdrop-blur-sm border border-white/30 text-white hover:bg-white/30 transition-all"
          aria-label={isAutoPlaying ? "Pause" : "Play"}
        >
          {isAutoPlaying ? (
            <div className="w-5 h-5 flex items-center justify-center">
              <div className="w-1 h-4 bg-white rounded mr-1" />
              <div className="w-1 h-4 bg-white rounded" />
            </div>
          ) : (
            <Play className="w-5 h-5 fill-current" />
          )}
        </button>
      </HStack>
      {/* Dots Indicators */}
      <div className="absolute bottom-2 left-1/2 -translate-x-1/2 flex gap-3 z-10">
        {slides.map((_, index) => (
          <button
            key={index}
            onClick={() => goToSlide(index)}
            className={`transition-all ${
              index === currentSlide
                ? "w-6 h-2 bg-white rounded-full"
                : "w-3 h-2 bg-white/50 rounded-full hover:bg-white/75"
            }`}
            aria-label={`Aller au slide ${index + 1}`}
          />
        ))}
      </div>
    </div>
  );
};

export default Hero;
