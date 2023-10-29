import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {SkyjoRoomIndexComponent} from "../skyjo/components/skyjo-room-index.component";
import {skyjoRoomGuard} from "../guards/skyjo-room-guard";
import {SkyjoRoomShowComponent} from "../skyjo/components/skyjo-room-show.component";

const routes: Routes = [
  {
    path: 'rooms',
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: SkyjoRoomIndexComponent
      },
      {
        path: ':roomId',
        canActivate: [skyjoRoomGuard],
        component: SkyjoRoomShowComponent
      }
    ]
  },
  {
    path: '',
    redirectTo: '/rooms',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: '/rooms'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
