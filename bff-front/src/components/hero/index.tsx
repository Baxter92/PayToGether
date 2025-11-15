import { useState, useEffect } from "react";
import { ChevronLeft, ChevronRight, Play, Sparkles } from "lucide-react";
import { Button } from "../ui/button";
import HStack from "../HStack";

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

interface HeroStats {
  value: string;
  label: string;
}

interface HeroProps {
  slides: Slide[];
  height?: { mobile?: string; desktop?: string };
  autoPlay?: boolean;
  autoPlayInterval?: number;
  pauseOnHover?: boolean;
  showArrows?: boolean;
  showDots?: boolean;
  showPlayPause?: boolean;
  arrowsPosition?: "top-right" | "top-left" | "center-sides" | "bottom-sides";
  dotsPosition?: "bottom-center" | "bottom-left" | "bottom-right";
  showBadge?: boolean;
  showStats?: boolean;
  stats?: HeroStats[];
  /** Accept either a Tailwind key ('2xl') or a CSS value like '900px' */
  maxContentWidth?: string;
  contentAlignment?: "left" | "center" | "right";
  transitionDuration?: number;
  transitionType?: "slide" | "fade";
  backgroundColor?: string;
  overlayOpacity?: number;
  onSlideChange?: (index: number) => void;
  onButtonClick?: (slide: Slide) => void;
}

const TAILWIND_MAXWIDTH_MAP: Record<string, string> = {
  xs: "max-w-xs",
  sm: "max-w-sm",
  md: "max-w-md",
  lg: "max-w-lg",
  xl: "max-w-xl",
  "2xl": "max-w-2xl",
  "3xl": "max-w-3xl",
  "4xl": "max-w-4xl",
  "5xl": "max-w-5xl",
  "6xl": "max-w-6xl",
  "7xl": "max-w-7xl",
};

const Hero = ({
  slides,
  height = { mobile: "400px", desktop: "500px" },
  autoPlay = true,
  autoPlayInterval = 5000,
  pauseOnHover = true,
  showArrows = true,
  showDots = true,
  showPlayPause = true,
  arrowsPosition = "top-right",
  dotsPosition = "bottom-center",
  showBadge = true,
  showStats = true,
  stats,
  maxContentWidth = "2xl",
  contentAlignment = "left",
  transitionDuration = 700,
  transitionType = "slide",
  backgroundColor = "bg-gray-900",
  overlayOpacity = 50,
  onSlideChange,
  onButtonClick,
}: HeroProps) => {
  const [currentSlide, setCurrentSlide] = useState(0);
  const [isAutoPlaying, setIsAutoPlaying] = useState(autoPlay);
  const [isHovered, setIsHovered] = useState(false);

  if (!slides || slides.length === 0) {
    return null;
  }

  const defaultStats: HeroStats[] = [
    { value: "5000+", label: "Offres disponibles" },
    { value: "98%", label: "Clients satisfaits" },
    { value: "24/7", label: "Support client" },
  ];
  const displayStats = stats || defaultStats;

  // Auto-play
  useEffect(() => {
    if (!isAutoPlaying || (pauseOnHover && isHovered)) return;

    const interval = setInterval(() => {
      setCurrentSlide((prev) => {
        const newIndex = (prev + 1) % slides.length;
        onSlideChange?.(newIndex);
        return newIndex;
      });
    }, autoPlayInterval);

    return () => clearInterval(interval);
  }, [
    isAutoPlaying,
    isHovered,
    pauseOnHover,
    slides.length,
    autoPlayInterval,
    onSlideChange,
  ]);

  const goToSlide = (index: number) => {
    const idx = Math.max(0, Math.min(index, slides.length - 1));
    setCurrentSlide(idx);
    onSlideChange?.(idx);
    if (autoPlay) {
      setIsAutoPlaying(false);
      setTimeout(() => setIsAutoPlaying(true), autoPlayInterval * 2);
    }
  };

  const nextSlide = () => goToSlide((currentSlide + 1) % slides.length);
  const prevSlide = () =>
    goToSlide((currentSlide - 1 + slides.length) % slides.length);

  const handleButtonClick = (slide: Slide) => {
    onButtonClick?.(slide);
  };

  // Height classes: using inline styles so Tailwind purge ne supprime rien
  const containerStyle = {
    height: height.mobile,
    // on md+ use desktop height via tailwind's md: class can't be inline style;
    // so we keep the JIT-friendly arbitrary value class too (h-[...] syntax)
  };
  const heightClass = `h-[${height.mobile}] md:h-[${height.desktop}]`;

  // content width: either map to Tailwind class or fallback to inline maxWidth
  const contentWidthTailwind = TAILWIND_MAXWIDTH_MAP[maxContentWidth];
  const contentStyle = contentWidthTailwind
    ? undefined
    : { maxWidth: maxContentWidth };

  const alignmentClass =
    contentAlignment === "center"
      ? "items-center text-center"
      : contentAlignment === "right"
      ? "items-end text-right"
      : "items-start text-left";

  const getArrowsContainerClass = () => {
    switch (arrowsPosition) {
      case "top-left":
        return "absolute top-4 left-4 z-20";
      case "center-sides":
        return "absolute top-1/2 -translate-y-1/2 w-full px-4 flex justify-between pointer-events-none z-20";
      case "bottom-sides":
        return "absolute bottom-4 w-full px-4 flex justify-between pointer-events-none z-20";
      default:
        return "absolute top-4 right-4 z-20";
    }
  };

  const getDotsContainerClass = () => {
    switch (dotsPosition) {
      case "bottom-left":
        return "absolute bottom-6 left-8 z-20";
      case "bottom-right":
        return "absolute bottom-6 right-8 z-20";
      default:
        return "absolute bottom-6 left-1/2 -translate-x-1/2 z-20";
    }
  };

  return (
    <div
      className={`relative w-full overflow-hidden ${backgroundColor} ${heightClass}`}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      style={containerStyle}
    >
      {/* Slides container */}
      <div className="relative w-full h-full">
        {/* Slide track for SLIDE transition */}
        {transitionType === "slide" && (
          <div
            className="flex h-full w-full"
            style={{
              transform: `translateX(-${currentSlide * 100}%)`,
              transitionProperty: "transform",
              transitionTimingFunction: "ease-out",
              transitionDuration: `${transitionDuration}ms`,
            }}
          >
            {slides.map((slide) => (
              <div key={slide.id} className="min-w-full h-full relative">
                {/* Use img to ensure object-cover behaviour */}
                <img
                  src={slide.image}
                  alt={slide.title}
                  className="absolute inset-0 w-full h-full object-cover"
                  // loading="lazy" peut être ajouté si souhaité
                />

                {/* Gradient overlay */}
                <div
                  className={`absolute inset-0 bg-gradient-to-r ${slide.gradient}`}
                  style={{
                    opacity: overlayOpacity / 100,
                    pointerEvents: "none",
                  }}
                />

                {/* Content area */}
                <div className="relative h-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                  <div className={`flex h-full ${alignmentClass}`}>
                    <div
                      className={`${
                        contentWidthTailwind ?? ""
                      } space-y-4 md:space-y-6 animate-fade-in`}
                      style={contentStyle}
                    >
                      {showBadge && slide.badge && (
                        <div
                          className={`inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white/20 backdrop-blur-sm border border-white/30 ${
                            contentAlignment === "center"
                              ? "mx-auto"
                              : contentAlignment === "right"
                              ? "ml-auto"
                              : ""
                          }`}
                        >
                          <Sparkles className="w-4 h-4" />
                          <span className="text-sm font-semibold">
                            {slide.badge}
                          </span>
                        </div>
                      )}

                      <p
                        className={`text-sm md:text-xl font-medium ${slide.textColor} uppercase tracking-wide`}
                      >
                        {slide.subtitle}
                      </p>

                      <h1
                        className={`text-3xl md:text-5xl lg:text-6xl font-bold ${slide.textColor} leading-tight`}
                      >
                        {slide.title}
                      </h1>

                      <p
                        className={`text-base md:text-lg lg:text-xl ${slide.textColor} opacity-90`}
                      >
                        {slide.description}
                      </p>

                      <div
                        className={
                          contentAlignment === "center"
                            ? "flex justify-center"
                            : contentAlignment === "right"
                            ? "flex justify-end"
                            : ""
                        }
                      >
                        <Button
                          className="bg-white text-gray-900 hover:bg-gray-100 rounded-full px-6 py-3 text-base"
                          onClick={() => handleButtonClick(slide)}
                          asChild={!onButtonClick}
                        >
                          {onButtonClick ? (
                            <span className="flex items-center gap-2">
                              {slide.buttonText}
                              <Play className="w-4 h-4 fill-current" />
                            </span>
                          ) : (
                            <a
                              href={slide.buttonLink}
                              className="flex items-center gap-2"
                            >
                              {slide.buttonText}
                              <Play className="w-4 h-4 fill-current" />
                            </a>
                          )}
                        </Button>
                      </div>

                      {showStats && displayStats.length > 0 && (
                        <div
                          className={`flex gap-6 md:gap-8 pt-2 md:pt-4 ${
                            contentAlignment === "center"
                              ? "justify-center"
                              : contentAlignment === "right"
                              ? "justify-end"
                              : ""
                          }`}
                        >
                          {displayStats.map((stat, idx) => (
                            <div key={idx}>
                              <p
                                className={`text-lg md:text-2xl font-bold ${slide.textColor}`}
                              >
                                {stat.value}
                              </p>
                              <p
                                className={`text-xs md:text-sm ${slide.textColor} opacity-80`}
                              >
                                {stat.label}
                              </p>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Fade variant */}
        {transitionType === "fade" &&
          slides.map((slide, index) => (
            <div
              key={slide.id}
              className="absolute inset-0 h-full w-full"
              style={{
                transitionProperty: "opacity",
                transitionDuration: `${transitionDuration}ms`,
                opacity: index === currentSlide ? 1 : 0,
                pointerEvents: index === currentSlide ? "auto" : "none",
              }}
            >
              <img
                src={slide.image}
                alt={slide.title}
                className="absolute inset-0 w-full h-full object-cover"
              />
              <div
                className={`absolute inset-0 bg-gradient-to-r ${slide.gradient}`}
                style={{ opacity: overlayOpacity / 100, pointerEvents: "none" }}
              />
              <div className="relative h-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className={`flex h-full ${alignmentClass}`}>
                  <div
                    className={`${
                      contentWidthTailwind ?? ""
                    } space-y-4 md:space-y-6`}
                    style={contentStyle}
                  >
                    {/* same content as above (badge, title, desc, button, stats) */}
                    <p
                      className={`text-sm md:text-xl font-medium ${slide.textColor} uppercase tracking-wide`}
                    >
                      {slide.subtitle}
                    </p>
                    <h1
                      className={`text-3xl md:text-5xl lg:text-6xl font-bold ${slide.textColor} leading-tight`}
                    >
                      {slide.title}
                    </h1>
                    <p
                      className={`text-base md:text-lg lg:text-xl ${slide.textColor} opacity-90`}
                    >
                      {slide.description}
                    </p>
                    <div
                      className={
                        contentAlignment === "center"
                          ? "flex justify-center"
                          : contentAlignment === "right"
                          ? "flex justify-end"
                          : ""
                      }
                    >
                      <Button
                        className="bg-white text-gray-900 hover:bg-gray-100 rounded-full px-6 py-3 text-base"
                        onClick={() => handleButtonClick(slide)}
                        asChild={!onButtonClick}
                      >
                        {onButtonClick ? (
                          <span className="flex items-center gap-2">
                            {slide.buttonText}
                            <Play className="w-4 h-4 fill-current" />
                          </span>
                        ) : (
                          <a
                            href={slide.buttonLink}
                            className="flex items-center gap-2"
                          >
                            {slide.buttonText}
                            <Play className="w-4 h-4 fill-current" />
                          </a>
                        )}
                      </Button>
                    </div>
                    {showStats && displayStats.length > 0 && (
                      <div
                        className={`flex gap-6 md:gap-8 pt-2 md:pt-4 ${
                          contentAlignment === "center"
                            ? "justify-center"
                            : contentAlignment === "right"
                            ? "justify-end"
                            : ""
                        }`}
                      >
                        {displayStats.map((stat, idx) => (
                          <div key={idx}>
                            <p
                              className={`text-lg md:text-2xl font-bold ${slide.textColor}`}
                            >
                              {stat.value}
                            </p>
                            <p
                              className={`text-xs md:text-sm ${slide.textColor} opacity-80`}
                            >
                              {stat.label}
                            </p>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))}
      </div>

      {/* Navigation Arrows */}
      {showArrows && (
        <div className={getArrowsContainerClass()}>
          {arrowsPosition === "center-sides" ||
          arrowsPosition === "bottom-sides" ? (
            <>
              <button
                onClick={prevSlide}
                className="pointer-events-auto p-3 rounded-full bg-white/20 backdrop-blur-sm border border-white/30 text-white hover:bg-white/30 transition-all"
                aria-label="Slide précédent"
              >
                <ChevronLeft className="w-6 h-6" />
              </button>
              <button
                onClick={nextSlide}
                className="pointer-events-auto p-3 rounded-full bg-white/20 backdrop-blur-sm border border-white/30 text-white hover:bg-white/30 transition-all"
                aria-label="Slide suivant"
              >
                <ChevronRight className="w-6 h-6" />
              </button>
            </>
          ) : (
            <HStack className="gap-2">
              <button
                onClick={prevSlide}
                className="p-2 rounded-full bg-white/20 backdrop-blur-sm border border-white/30 text-white hover:bg-white/30 transition-all"
                aria-label="Slide précédent"
              >
                <ChevronLeft className="w-5 h-5" />
              </button>
              <button
                onClick={nextSlide}
                className="p-2 rounded-full bg-white/20 backdrop-blur-sm border border-white/30 text-white hover:bg-white/30 transition-all"
                aria-label="Slide suivant"
              >
                <ChevronRight className="w-5 h-5" />
              </button>
              {showPlayPause && (
                <button
                  onClick={() => setIsAutoPlaying(!isAutoPlaying)}
                  className="p-2 rounded-full bg-white/20 backdrop-blur-sm border border-white/30 text-white hover:bg-white/30 transition-all"
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
              )}
            </HStack>
          )}
        </div>
      )}

      {/* Dots */}
      {showDots && (
        <div className={`${getDotsContainerClass()} flex gap-3 z-30`}>
          {slides.map((_, index) => (
            <button
              key={index}
              onClick={() => goToSlide(index)}
              className={`transition-all ${
                index === currentSlide
                  ? "w-8 h-2 bg-white rounded-full"
                  : "w-2 h-2 bg-white/50 rounded-full hover:bg-white/75"
              }`}
              aria-label={`Aller au slide ${index + 1}`}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default Hero;
