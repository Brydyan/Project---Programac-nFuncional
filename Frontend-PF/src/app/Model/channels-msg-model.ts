export interface ChannelsMsgModel {
    id: string;
    messageId?: string;
    channel: string;
    senderId: string;
    messageContent: string;
    timestamp: string;
    status: string;
}
