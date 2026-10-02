package com.maxence.hxh.ability;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Registre simple des Hatsu, identique côté client et serveur. */
public final class NenAbilities {
    private static final Map<String, AbstractNenAbility> ABILITIES = new LinkedHashMap<>();

    public static final BigBangImpactAbility BIG_BANG_IMPACT = register(new BigBangImpactAbility());
    // À venir : NenBulletAbility (Émission), ChainJailAbility (Matérialisation),
    // KanmuruAbility (Transformation), NeedleControlAbility (Manipulation)...

    private static <T extends AbstractNenAbility> T register(T ability) {
        ABILITIES.put(ability.getId(), ability);
        return ability;
    }

    public static AbstractNenAbility get(String id) {
        return ABILITIES.get(id);
    }

    public static Collection<AbstractNenAbility> all() {
        return Collections.unmodifiableCollection(ABILITIES.values());
    }

    private NenAbilities() {}
}
