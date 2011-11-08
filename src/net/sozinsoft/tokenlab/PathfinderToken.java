package net.sozinsoft.tokenlab;


import net.rptools.maptool.model.*;
import net.rptools.maptool.util.PersistenceUtil;
import net.rptools.maptool.util.TokenUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class PathfinderToken {

    private static final String CLASS_HP = "ClassHP";
    public static final String MISC_HP = "MiscHP";
    private static final String AC_ENH_BONUS = "ACEnhBonus";
    private static final String SIZE_MOD = "SizeMod";
    private static final String AC_TEMP_BONUS = "ACTempBonus";
    private static final String AC_MISC_BONUS_2 = "ACMiscBonus2";
    private static final String AC_MISC_BONUS_1 = "ACMiscBonus1";
    private static final String AC_FEAT_BONUS = "ACFeatBonus";
    private static final String AC_CLASS_BONUS = "ACClassBonus";
    private static final String AC_SHIELD_BONUS = "ACShieldBonus";
    private static final String AC_ARMOR_BONUS = "ACArmorBonus";
    private static final String ARMOR_MAX_DEX_BONUS = "ArmorMaxDexBonus";
    private static final String ARMOR_CHECK_PENALTY = "ArmorCheckPenalty";
    private static final String MISC_BONUS1 = "MiscBonus1";
    private static final String MISC_BONUS2 = "MiscBonus2";
    private static final String RES_TOKEN_LAB_MACRO_SET_MTMACSET = "res/TokenLabMacroSet.mtmacset";
    private static final String DAMAGE = "Damage";
    private static final String ENHANCEMENT = "Enhancement";
    private static final String CLASS_BONUS = "ClassBonus";
    private static final String FEAT_BONUS = "FeatBonus";
    private static final String ENH_BONUS = "EnhBonus";
    private static final String RANKS = "Ranks";
    private static final String BONUS = "Bonus";
    private static final String CLASS_SKILL = "ClassSkill";
    private static final String PATHFINDER = "Pathfinder";
    private static final String CHARACTER = "Character";
    private static final String RACE = "Race";
    private static final String PLAYER = "Player";
    private static final String ALIGNMENT = "Alignment";
    private static final String DEITY = "Deity";
    private static final String CLASS = "Class";
    private static final String LEVEL = "Level";
    private static final String GENDER = "Gender";
    private static final String AGE = "Age";
    private static final String HEIGHT = "Height";
    private static final String WEIGHT = "Weight";
    private static final String SPEED = "Speed";
    private static final String BASE_ATTACK_BONUS = "BaseAttackBonus";
    private static final String INIT_MOD = "InitMod";
    private Character _character;

    public PathfinderToken( Character character ) {
        _character = character;
    }

    public Token asToken( Config.ConfigEntry configEntry ) throws IOException {

        Token t = createToken(configEntry);
        loadMacros(t);

        setCoreProperties(t);


        setCharacterAttributes(t);
        setPenalties(t);

        setHitPoints(t);
        setArmorClass(t);


        t.setProperty(BASE_ATTACK_BONUS, _character.getBaseAttackBonus());
		t.setProperty(INIT_MOD, Integer.toString( _character.getInitiative().initModifier() ) );

        setSavingThrows(t);
        setSkills(t);

        return t;
    }

    private void setCoreProperties(Token t) {
        t.setPropertyType(PATHFINDER);
        t.setProperty(CHARACTER, _character.getName());
        t.setProperty(RACE, _character.getRace());
        t.setProperty(PLAYER, _character.getPlayerName());
        t.setProperty(ALIGNMENT, _character.getAlignment() );
        t.setProperty(DEITY, _character.getDeity());
        t.setProperty(CLASS, _character.getClassSummaryAbbreviation());
        t.setProperty(LEVEL, _character.getLevel());
        t.setProperty(GENDER, _character.getGender());
        t.setProperty(AGE, _character.getAge());
        t.setProperty(HEIGHT, _character.getHeight());
        t.setProperty(WEIGHT, _character.getWeight());
        t.setProperty(SPEED, _character.getSpeed() );

        if (_character.isNPC()) {
            t.setType(Token.Type.NPC);
        } else {
            t.setType(Token.Type.PC);
        }

    }

    private void setSkills(Token t) {
        for( Character.Skill skill: _character.getSkills().values()) {
            t.setProperty( skill.skillName + RANKS, skill.ranks );
            t.setProperty( skill.skillName + BONUS, skill.attrBonus);
            if ( skill.isClassSkill == true ) {
                t.setProperty( skill.skillName + CLASS_SKILL, "1");
            }
        }
    }

    private void setSavingThrows(Token t) {
        HashMap<String, Character.Save> saves = _character.getSaves();
        for (String saveName : saves.keySet()) {
            Character.Save s = saves.get(saveName);
            t.setProperty(s.abbr + CLASS_BONUS, s.base);
            t.setProperty(s.abbr + FEAT_BONUS, s.frommisc);
            t.setProperty(s.abbr + ENH_BONUS, s.fromresist);
            String miscBonus1 = s.abbr + MISC_BONUS1;
            if (t.getProperty(miscBonus1) == null) {
                t.setProperty(miscBonus1, "0");
            }
            String miscBonus2 = s.abbr + MISC_BONUS2;
            if (t.getProperty(miscBonus2) == null) {
                t.setProperty(miscBonus2, "0");
            }
        }
    }

    private void setArmorClass(Token t) {
        Character.ArmorClass AC = _character.getArmorClass();
        t.setProperty(AC_ARMOR_BONUS, AC.fromarmor);
        t.setProperty(AC_SHIELD_BONUS, AC.fromshield);
        t.setProperty(AC_CLASS_BONUS, "0"); //TODO: does herolabs ever define this?
        t.setProperty(AC_FEAT_BONUS, AC.fromdodge); //TODO: anything other than dodge here?
        t.setProperty(AC_MISC_BONUS_1, AC.frommisc);
        t.setProperty(AC_MISC_BONUS_2, "0");
        t.setProperty(AC_TEMP_BONUS, "0");
        t.setProperty(SIZE_MOD, AC.fromsize);

        //finally do the enhancement bonus
        int enhancementbonus = Integer.parseInt(AC.fromdeflect) +
                Integer.parseInt(AC.fromnatural);
        t.setProperty(AC_ENH_BONUS, Integer.toString(enhancementbonus));
    }

	private void setHitPoints(Token t) {
		CharacterAttribute con = _character.getConstitution();
		int level = Integer.parseInt( _character.getLevel() );
		int hp = Integer.parseInt( _character.getHitpoints() );
		int conBonus = con.getBonus();
		int baseHitpoints = hp - conBonus * level;
		t.setProperty( CLASS_HP, Integer.toString( baseHitpoints ) );
		t.setProperty(MISC_HP, Integer.toString( hp - baseHitpoints ) );
	}

    private void setPenalties(Token t) {
        //penalties
        Character.Penalty dexPenalty = _character.getPenalty("Max Dex Bonus"); //penalties.get( "Max Dex Bonus");
        if ( dexPenalty != null ) {
            t.setProperty(ARMOR_MAX_DEX_BONUS, dexPenalty.value);
        }
        Character.Penalty armorCheckPenalty = _character.getPenalty("Armor Check Penalty");
        if ( armorCheckPenalty != null ) {
            t.setProperty(ARMOR_CHECK_PENALTY, armorCheckPenalty.value);
        }
    }

    private void setCharacterAttributes(Token t) {
        for (CharacterAttribute ca : _character.getAttributes().values()) {
            t.setProperty(ca.getName(), ca.getValue());
            String enhancementProperty = CharacterAttribute.getShortName(ca.getName()) + ENHANCEMENT;
            t.setProperty(enhancementProperty, Integer.toString((int) Math.floor(ca.getEnhancementBonus() / 2))); //TODO: this is ugly
            String damageProperty = CharacterAttribute.getShortName(ca.getName()) + DAMAGE;
            t.setProperty(damageProperty, "0");
            if (ca.isVoid()) { //void attributes mess up saves, so...
                String saveShortName = CharacterAttribute.getSaveShortName(ca.getName());
                if (saveShortName != null) {
                    String attrib = saveShortName + MISC_BONUS1;
                    t.setProperty(attrib, Integer.toString(ca.getModifiedEnhancementBonus()));
                    attrib = saveShortName + MISC_BONUS2;
                    t.setProperty(attrib, "-1"); //TODO: this is a bit of a hack to work around the PF ruleset.
                }
            }
        }
    }

    private Token createToken(Config.ConfigEntry configEntry) throws IOException {
        Asset tokenImage = null;
        File file = new File( configEntry.getImageFilePath());
        tokenImage = AssetManager.createAsset(file);
        AssetManager.putAsset( tokenImage );
        Token t = new Token( _character.getName(), tokenImage.getId());

        t.setImageAsset(tokenImage.getName());
        t.setImageAsset(tokenImage.getName(), tokenImage.getId());

        //set the image shape
        Image image = ImageIO.read(file);
        t.setShape(TokenUtil.guessTokenType(image));


		//set the token size
        String characterSize = _character.getSize();
		if ( characterSize != null && ! characterSize.isEmpty()) {
			SquareGrid grid = new SquareGrid();
			for (TokenFootprint footprint : grid.getFootprints()) {
				if ( characterSize.equals( footprint.getName() ) ) {
					t.setFootprint( grid, footprint );
				}
			}
		}

        //set the other image assets (portrait, charsheet image)
        Asset portrait = AssetManager.createAsset( new File( configEntry.getPortraitFilePath()) );
        AssetManager.putAsset(portrait);
        t.setPortraitImage(portrait.getId());
        t.setCharsheetImage(portrait.getId());

        return t;
    }


    private void loadMacros(Token t) throws IOException {
        List<MacroButtonProperties> macroButtonSet = PersistenceUtil.loadMacroSet(
                new File(this.getClass().getResource(RES_TOKEN_LAB_MACRO_SET_MTMACSET).getFile()));
        t.getMacroNextIndex(); //TODO: this is a hack to create the underlying macroPropertiesMap hash table
        t.replaceMacroList(macroButtonSet);

	}
}