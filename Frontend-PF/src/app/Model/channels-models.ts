import { MemberModel } from "./member-model";

export interface ChannelsModel {
    id: string;
    name: string;
    type: string;        // PUBLIC | PRIVATE
    ownerId: string;
    members: string[];
    createdAt: string;   // o Date si quieres transformarlo

    unread?: number;
    membersInfo?: MemberModel[];  // agregar
}