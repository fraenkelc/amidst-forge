Amidst for Forge
================

[![Build Status](https://travis-ci.org/fraenkelc/amidst-forge.svg?branch=master)](https://travis-ci.org/fraenkelc/amidst-forge)

Client-Side only mod that integrates all modded biomes into the [Amidst application](https://github.com/toolbox4minecraft/amidst). 


How to use
----------

![picture showing the amidst button in the option menu](images/options.png)

### Free seed selection

This mod adds the following ways of starting amidst: 
* the button "Launch Amidst" in the options menu 
* the command `/amidst`

Start Amidst using one of these methods. Once started you can select a seed using the Amidst functions "New from seed" and "New from random seed"

### Overworld seed & generation

Some Mods (e.g. GeographiCraft) tie their world generation to a specific dimension. The button "Amidst Overworld" adds in rudimentary support for these mods. This button is enabled once you've created / joined a local singleplayer world.
Once started use the function "New from seed" / "New from random seed" to open the map.
> Note: the entered and displayed seed value is wrong and is ignored. The "Amidst Overworld" feature uses the seed of the currently running minecraft world.    

Known issues
------------

### Mod compatibility
Some general points on Mod compatibility:
 * Note: Some mods (e.g. Traverse) only add their biomes during server start. If you encounter differences between amidst output and the minecraft world try to start a single  player world with corresponding settings first. This is especially relevant with mods that add versioned biomes.
 *  Due to the way Amidst works changes to the generations of structures and features will *not* be reflected correctly in the Amidst map.

| Mod | Known issues |
|-----|--------------|
|[Traverse](https://minecraft.curseforge.com/projects/traverse)| Biomes are only added to generation once a world is loaded. Create a new world and use "Launch Amidst" after you joined the world |
|[Climate Control/Geographicraft](https://minecraft.curseforge.com/projects/climate-control-geographicraft)| Geographicraft ties its generation to the worlds dimensions. Create a world with the desired seed, then use "Amidst Overworld" to explore that seed. Feature / Structure locations are not listed correctly on the map |
|[OpenTerrainGenerator](https://minecraft.curseforge.com/projects/open-terrain-generator)| OTG supports more than 256 biomes by using a feature called "replaceToBiomeName". Worlds using this feature will not open in Amidst because it's not possible to map all biomes to the ID range supported by Amidst. This affects for example the [Biome Bundle](https://minecraft.curseforge.com/projects/biome-bundle) mod.|


### Miscellaneous

Some functions of the launched Amidst application such as "Switch profile" and "Open saved game" may not work and cause exceptions. For further issues please check the [issue tracker](../../issues)
