import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {RoomIndexComponent} from "./room-index/room-index.component";
import {roomGuard} from "../guards/room.guard";
import {RoomShowComponent} from "./room-show/room-show.component";

const routes: Routes = [
  {
    path: 'rooms',
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: RoomIndexComponent
      },
      {
        path: ':roomId',
        canActivate: [roomGuard],
        component: RoomShowComponent
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
