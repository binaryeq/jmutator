package binaryeq.jmutator;

import com.google.common.base.Preconditions;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.config.Mutator;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MethodMutatorFactoryFactory {
    private static final Map<String, Supplier<Collection<MethodMutatorFactory>>> MUTATOR_FACTORIES = Map.of(
            "all", Mutator::all,
            "defaults", Mutator::newDefaults,
            "allnonexperimental", MethodMutatorFactoryFactory::allNonExperimentalMutators
    );

    public static Collection<MethodMutatorFactory> getMethodMutatorFactories(String id) {
        Preconditions.checkArgument(MUTATOR_FACTORIES.containsKey(id), "Unknown method mutator factory ID '" + id + "'");
        return MUTATOR_FACTORIES.get(id).get();
    }

    public static String getDefaultMethodMutatorFactoriesId() {
        return "allnonexperimental";
    }

    public static Set<String> getMethodMutatorFactoryIds() {
        return MUTATOR_FACTORIES.keySet();
    }

    // Additional factories
    public static Collection<MethodMutatorFactory> allNonExperimentalMutators() {
        return Mutator.all().stream().filter(m -> !m.getName().contains("EXPERIMENTAL")).collect(Collectors.toList());
    }
}
