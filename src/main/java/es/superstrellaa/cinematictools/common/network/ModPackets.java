package es.superstrellaa.cinematictools.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class ModPackets {
    public static final CustomPacketPayload.Type<CheckOpPayload> CHECK_OP_TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("cinematictools", "check_op"));

    public static final StreamCodec<FriendlyByteBuf, CheckOpPayload> CHECK_OP_CODEC =
            StreamCodec.of(CheckOpPayload::write, CheckOpPayload::read);

    public record CheckOpPayload(boolean isOp) implements CustomPacketPayload {
        public static CheckOpPayload read(FriendlyByteBuf buf) {
            return new CheckOpPayload(buf.readBoolean());
        }

        public static void write(FriendlyByteBuf buf, CheckOpPayload payload) {
            buf.writeBoolean(payload.isOp);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return CHECK_OP_TYPE;
        }
    }
}
