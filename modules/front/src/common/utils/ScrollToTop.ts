import { useLayoutEffect } from "react";
import { useLocation } from "react-router-dom";

export default function ScrollToTop(): null {
  const { pathname } = useLocation();

  useLayoutEffect(() => {
    // instant scroll to top on route change
    window.scrollTo(0, 0);
  }, [pathname]);

  return null;
}
