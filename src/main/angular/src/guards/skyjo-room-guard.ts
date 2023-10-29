import {ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot} from "@angular/router";
import {inject} from "@angular/core";
import {SkyjoService} from "../skyjo/services/skyjo.service";

// Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree
export const skyjoRoomGuard: CanActivateFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  const roomStateService = inject(SkyjoService);
  const router = inject(Router);
  const roomId = route.params['roomId'];

  return new Promise(async accept => {
    try {
      await roomStateService.refreshCurrentRoom(roomId);
      accept(true);
    }
    catch {
      await router.navigate(['/rooms']);
      accept(false);
    }
  })
};
