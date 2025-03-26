package com.montaury.pokebagarre.metier;

import com.montaury.pokebagarre.erreurs.ErreurMemePokemon;
import com.montaury.pokebagarre.erreurs.ErreurPokemonNonRenseigne;
import com.montaury.pokebagarre.erreurs.ErreurRecuperationPokemon;
import com.montaury.pokebagarre.webapi.PokeBuildApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/*
*
*
* */

class BagarreTest {
    private PokeBuildApi fausseApi;
    private Bagarre bagarre;

    @BeforeEach
    public void setUp(){
        fausseApi = Mockito.mock(PokeBuildApi.class);
        bagarre = new Bagarre(fausseApi);
    }



    @Test
    void devrait_lever_une_exception_si_le_nom_du_second_pokemon_non_renseigne(){
        // GIVEN
        Pokemon pokemon1 = new Pokemon("Pikachu", null, null);
        Pokemon pokemon2 = new Pokemon(null, null, null);
        // WHEN
        Throwable thrown = catchThrowable(() -> bagarre.validerNomPokemons(pokemon1.getNom(), pokemon2.getNom()));
        // THEN
        assertThat(thrown)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le second pokemon n'est pas renseigne");

    }

    @Test
    void devrait_lever_une_exception_si_le_nom_du_premier_pokemon_non_renseigne(){
        // GIVEN
        Pokemon pokemon1 = new Pokemon(null, null, null);
        Pokemon pokemon2 = new Pokemon("Dracaufeu", null, null);
        // WHEN
        Throwable thrown = catchThrowable(() -> bagarre.validerNomPokemons(pokemon1.getNom(), pokemon2.getNom()));
        // THEN
        assertThat(thrown)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le premier pokemon n'est pas renseigne");

    }

    @Test
    void devrait_lever_une_exception_si_le_nom_du_second_pokemon_vide(){
        // GIVEN
        Bagarre bagarre = new Bagarre();
        Pokemon pokemon1 = new Pokemon("Pikachu", null, null);
        Pokemon pokemon2 = new Pokemon("", null, null);
        // WHEN
        Throwable thrown = catchThrowable(() -> bagarre.validerNomPokemons(pokemon1.getNom(), pokemon2.getNom()));
        // THEN
        assertThat(thrown)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le second pokemon n'est pas renseigne");

    }

    @Test
    void devrait_lever_une_exception_si_le_nom_du_premier_pokemon_vide(){
        // GIVEN
        Pokemon pokemon1 = new Pokemon("", null, null);
        Pokemon pokemon2 = new Pokemon("Dracaufeu", null, null);
        // WHEN
        Throwable thrown = catchThrowable(() -> bagarre.validerNomPokemons(pokemon1.getNom(), pokemon2.getNom()));
        // THEN
        assertThat(thrown)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le premier pokemon n'est pas renseigne");

    }

    @Test
    void devrait_lever_une_exception_si_les_noms_des_pokemons_sont_identiques(){
        //GIVEN
        Pokemon pokemon1 = new Pokemon("Dracaufeu", null, null);
        Pokemon pokemon2 = new Pokemon("Dracaufeu", null, null);
        // WHEN
        Throwable thrown = catchThrowable(() -> bagarre.validerNomPokemons(pokemon1.getNom(), pokemon2.getNom()));
        // THEN
        assertThat(thrown)
                .isInstanceOf(ErreurMemePokemon.class)
                .hasMessage("Impossible de faire se bagarrer un pokemon avec lui-meme");
    }

    @Test
    void devrait_lever_une_exception_si_le_pokemon_un_pas_trouve_dans_api() {
        // GIVEN
        String nom_pokemon_1 = "Pikachu";
        String nom_pokemon_2 = "Dracaufeu";
        Mockito.when(fausseApi.recupererParNom(nom_pokemon_1)).thenReturn(CompletableFuture.failedFuture(new ErreurRecuperationPokemon(nom_pokemon_1)));
        Mockito.when(fausseApi.recupererParNom(nom_pokemon_2)).thenReturn(CompletableFuture.completedFuture(new Pokemon("Dracaufeu", null, null)));

        // WHEN
        CompletableFuture<Pokemon> futurResult = bagarre.demarrer(nom_pokemon_1, nom_pokemon_2);
        //THEN
        assertThat(futurResult)
                .failsWithin(Duration.ofSeconds(2))
                .withThrowableOfType(ExecutionException.class)
                .havingCause()
                .isInstanceOf(ErreurRecuperationPokemon.class)
                .withMessage("Impossible de recuperer les details sur '" + nom_pokemon_1 + "'") ;
    }

    @Test
    void devrait_lever_une_exception_si_le_pokemon_deux_pas_trouve_dans_api() {
        // GIVEN
        String nom_pokemon_1 = "Pikachu";
        String nom_pokemon_2 = "Dracaufeu";
        Mockito.when(fausseApi.recupererParNom(nom_pokemon_1)).thenReturn(CompletableFuture.completedFuture(new Pokemon("Pikachu", null, null)));
        Mockito.when(fausseApi.recupererParNom(nom_pokemon_2)).thenReturn(CompletableFuture.failedFuture(new ErreurRecuperationPokemon(nom_pokemon_2)));

        // WHEN
        CompletableFuture<Pokemon> futurResult = bagarre.demarrer(nom_pokemon_1, nom_pokemon_2);
        // THEN
        assertThat(futurResult)
                .failsWithin(Duration.ofSeconds(2))
                .withThrowableOfType(ExecutionException.class)
                .havingCause()
                .isInstanceOf(ErreurRecuperationPokemon.class)
                .withMessage("Impossible de recuperer les details sur '" + nom_pokemon_2 + "'") ;
    }

    @Test
    void devrait_retourner_le_premier_pokemon_vainqueur() {
        // GIVEN
        String nom_pokemon_1 = "Pikachu";
        String nom_pokemon_2 = "Dracaufeu";
        Mockito.when(fausseApi.recupererParNom(nom_pokemon_1)).thenReturn(CompletableFuture.completedFuture(new Pokemon(nom_pokemon_1, "url1", new Stats(2,2))));
        Mockito.when(fausseApi.recupererParNom(nom_pokemon_2)).thenReturn(CompletableFuture.completedFuture(new Pokemon(nom_pokemon_2, "url2", new Stats(1,1))));

        // WHEN
        CompletableFuture<Pokemon> futurVainqueur = bagarre.demarrer(nom_pokemon_1, nom_pokemon_2);
        // THEN
        assertThat(futurVainqueur)
                .succeedsWithin(Duration.ofSeconds(2))
                .satisfies(pokemon -> {
                    assertThat(pokemon.getNom()).isEqualTo(nom_pokemon_1);
                    assertThat(pokemon.getUrlImage()).isEqualTo("url1");
                    assertThat(pokemon.getStats().getAttaque()).isEqualTo(2);
                    assertThat(pokemon.getStats().getDefense()).isEqualTo(2);
                });
    }

    @Test
    void devrait_retourner_le_second_pokemon_vainqueur() {
        // GIVEN
        String nom_pokemon_1 = "Pikachu";
        String nom_pokemon_2 = "Dracaufeu";
        Mockito.when(fausseApi.recupererParNom(nom_pokemon_1)).thenReturn(CompletableFuture.completedFuture(new Pokemon(nom_pokemon_1, "url1", new Stats(1,1))));
        Mockito.when(fausseApi.recupererParNom(nom_pokemon_2)).thenReturn(CompletableFuture.completedFuture(new Pokemon(nom_pokemon_2, "url2", new Stats(2,2))));

        // WHEN
        CompletableFuture<Pokemon> futurVainqueur = bagarre.demarrer(nom_pokemon_1, nom_pokemon_2);
        // THEN
        assertThat(futurVainqueur)
                .succeedsWithin(Duration.ofSeconds(2))
                .satisfies(pokemon -> {
                    assertThat(pokemon.getNom()).isEqualTo(nom_pokemon_2);
                    assertThat(pokemon.getUrlImage()).isEqualTo("url2");
                    assertThat(pokemon.getStats().getAttaque()).isEqualTo(2);
                    assertThat(pokemon.getStats().getDefense()).isEqualTo(2);
                });
    }

}