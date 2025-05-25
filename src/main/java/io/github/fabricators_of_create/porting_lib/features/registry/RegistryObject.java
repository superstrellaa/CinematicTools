package io.github.fabricators_of_create.porting_lib.features.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("CanBeRecord")
public final class RegistryObject<T> implements Supplier<T> {

    private static final RegistryObject<?> EMPTY = new RegistryObject<>();
    private final ResourceLocation id;
    @Nullable
    private final ResourceKey<T> key;
    private Supplier<? extends T> getter;
    private T value;
    @Nullable
    private Holder<T> holder;

    private RegistryObject() {
        this.id = null;
        this.key = null;
    }

    public RegistryObject(ResourceLocation id, ResourceKey<?> key) {
        this.id = id;
        this.key = (ResourceKey<T>) key;
    }

    private static <T> RegistryObject<T> empty() {
        @SuppressWarnings("unchecked")
        RegistryObject<T> t = (RegistryObject<T>) EMPTY;
        return t;
    }

    private static boolean registryExists(ResourceLocation registryName) {
        return BuiltInRegistries.REGISTRY.containsKey(registryName);
    }

    void setValue(T value) {
        this.value = value;
    }

    void setGetter(Supplier<? extends T> getter) {
        this.getter = getter;
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public T get() {
        if (this.value == null)
            this.value = getter.get();
        return this.value;
    }

    @Nullable
    public ResourceKey<T> getKey() {
        return this.key;
    }

    public Stream<T> stream() {
        return isPresent() ? Stream.of(get()) : Stream.of();
    }

    public boolean isPresent() {
        return this.value != null;
    }

    public void ifPresent(Consumer<? super T> consumer) {
        if (isPresent())
            consumer.accept(get());
    }

    public RegistryObject<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent())
            return this;
        else
            return predicate.test(get()) ? this : empty();
    }

    public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return Optional.empty();
        else {
            return Optional.ofNullable(mapper.apply(get()));
        }
    }

    public <U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return Optional.empty();
        else {
            return Objects.requireNonNull(mapper.apply(get()));
        }
    }

    public <U> Supplier<U> lazyMap(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return () -> isPresent() ? mapper.apply(get()) : null;
    }

    public T orElse(T other) {
        return isPresent() ? get() : other;
    }

    public T orElseGet(Supplier<? extends T> other) {
        return isPresent() ? get() : other.get();
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (isPresent()) {
            return get();
        } else {
            throw exceptionSupplier.get();
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public Optional<Holder<T>> getHolder() {
        if (this.holder == null && this.key != null && registryExists(this.key.registry())) {
            ResourceLocation registryName = this.key.registry();
            Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(registryName);

            if (registry != null)
                this.holder = registry.getHolder(this.key).orElse(null);
        }

        return Optional.ofNullable(this.holder);
    }
}
