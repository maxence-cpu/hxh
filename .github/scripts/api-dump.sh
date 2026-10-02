#!/usr/bin/env bash
# Diagnostic : extrait les signatures de l'API Minecraft/NeoForge utilisée par le mod.
set +e
OUT=api.txt
: > $OUT
JAR=""
for j in $(find build ~/.gradle -name "*.jar" -size +20M 2>/dev/null); do
  if unzip -l "$j" 2>/dev/null | grep -q "net/minecraft/client/Minecraft.class"; then JAR="$j"; break; fi
done
NEO=$(find ~/.gradle build -name "neoforge-*-universal.jar" -o -name "neoforge-*.jar" 2>/dev/null | head -5)
echo "MC JAR: $JAR" >> $OUT
echo "NEO JARS: $NEO" >> $OUT
CP="$JAR:$(echo $NEO | tr ' ' ':')"
section() { echo -e "\n===== $1 =====" >> $OUT; }
dump() { section "$1"; javap -cp "$CP" -public "$1" >> $OUT 2>&1; }
dumpg() { section "$1 | $2"; javap -cp "$CP" -public "$1" 2>&1 | grep -iE "$2" >> $OUT; }

section "classes client/gui (racine)"
unzip -l "$JAR" | awk '{print $4}' | grep -E "^net/minecraft/client/gui/[A-Za-z]+\.class$" >> $OUT
section "classes Spider*"
unzip -l "$JAR" | awk '{print $4}' | grep -iE "spider" | grep -v '\$' >> $OUT
section "classes *GuiLayer* / *Layer* neoforge"
for n in $NEO; do unzip -l "$n" | awk '{print $4}' | grep -iE "GuiLayer|guilayer|RegisterGui" >> $OUT; done

dump net.minecraft.client.gui.screens.Screen
dumpg net.minecraft.client.Minecraft "screen|gui|options|player "
dump 'com.mojang.blaze3d.platform.InputConstants$Type'
dumpg com.mojang.blaze3d.platform.InputConstants "KEY_H|KEY_R|KEY_Z|KEY_G|Key "
dump net.minecraft.client.KeyMapping
dumpg net.minecraft.client.Options "hide|gui"
dumpg net.minecraft.world.entity.LivingEntity "swing|knockback"
dumpg net.minecraft.server.level.ServerPlayer "message|overlay|actionbar"
dumpg net.minecraft.world.entity.player.Player "message|overlay|actionbar"
dumpg net.minecraft.world.level.Level "random"
dumpg net.minecraft.world.entity.Entity "random"
dump net.minecraft.client.renderer.entity.MobRenderer
dump net.neoforged.neoforge.client.event.RegisterGuiLayersEvent
dumpg net.minecraft.client.gui.components.Button "builder|render|extract"
dumpg net.minecraft.client.gui.components.AbstractWidget "render|extract"
# Classe qui remplace GuiGraphics : on cherche la première classe client/gui ayant fill(...) et drawString/text
for c in $(unzip -l "$JAR" | awk '{print $4}' | grep -E "^net/minecraft/client/gui/[A-Za-z]+\.class$" | sed 's/\.class$//; s#/#.#g'); do
  if javap -cp "$CP" -public "$c" 2>/dev/null | grep -q " fill(int, int, int, int, int)"; then dump "$c"; fi
done
dumpg net.minecraft.client.gui.Gui "screen|hide|Screen"
dumpg net.minecraft.client.Minecraft "Screen"
section "GuiLayer (source)"; for n in $NEO; do unzip -p "$n" net/neoforged/neoforge/client/gui/GuiLayer.java 2>/dev/null | grep -vE "^\s*(\*|/)" ; done >> $OUT
javap -cp "$CP" -protected net.minecraft.client.model.monster.spider.SpiderModel >> $OUT 2>&1
dumpg net.minecraft.client.model.geom.ModelLayers "SPIDER"
section "LivingEntityRenderer (protected)"; javap -cp "$CP" -protected net.minecraft.client.renderer.entity.LivingEntityRenderer | grep -iE "texture|createRenderState|LivingEntityRenderer\(" >> $OUT
section "Mob (protected) ai"; javap -cp "$CP" -protected net.minecraft.world.entity.Mob | grep -iE "customServerAiStep|registerGoals|getAmbientSound" >> $OUT
section "Entity hurtMarked"; javap -cp "$CP" -public net.minecraft.world.entity.Entity | grep -iE "hurtMarked|setDeltaMovement|hurtServer" >> $OUT
