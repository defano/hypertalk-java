package hypercard.paint.model;

import java.util.Observable;
import java.util.Observer;

/**
 * Provides an {@link Observable} interface to a variable whose value may change over time, but whose value cannot be
 * changed by this provider.
 *
 * @param <T> The type of value being provided.
 */
public class ImmutableProvider<T> extends Observable implements Observer {

    protected T value;

    private ImmutableProvider source;
    private ProviderTransform transform;

    public ImmutableProvider() {
        source = null;
        transform = null;
        value = null;
    }

    public ImmutableProvider(T value) {
        this.value = value;
        this.transform = null;
        this.source = null;
    }

    private <S> ImmutableProvider(ImmutableProvider<S> derivedFrom, ProviderTransform<S, T> transform) {
        this.transform = transform;

        setSource(derivedFrom);
        update(derivedFrom, derivedFrom.get());
    }

    public static <S, T> ImmutableProvider<T> derivedFrom(ImmutableProvider<S> derivedFrom, ProviderTransform<S, T> transform) {
        return new ImmutableProvider<>(derivedFrom, transform);
    }

    public static <T> ImmutableProvider<T> from(Provider<T> derivedFrom) {
        return derivedFrom(derivedFrom, null);
    }

    public T get() {
        return value;
    }

    public void setSource(ImmutableProvider source) {
        if (source != null) {
            source.deleteObserver(this);
        }

        this.source = source;
        this.source.addObserver(this);
        update(this, source.get());
    }

    @Override
    public void update(Observable o, Object arg) {
        value = (transform == null) ? (T) arg : (T) transform.transform(arg);

        setChanged();
        notifyObservers(value);
    }

    public void addObserverAndUpdate(Observer o) {
        super.addObserver(o);
        o.update(this, value);
    }
}
