import {ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot} from "@angular/router";
import {inject} from "@angular/core";
import {RoomStateService} from "../services/room-state.service";

// Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree
export const roomGuard: CanActivateFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  const roomStateService = inject(RoomStateService);
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
