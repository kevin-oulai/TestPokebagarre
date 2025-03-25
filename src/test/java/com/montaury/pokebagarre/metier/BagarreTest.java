package com.montaury.pokebagarre.metier;

import com.montaury.pokebagarre.erreurs.ErreurMemePokemon;
import com.montaury.pokebagarre.erreurs.ErreurPokemonNonRenseigne;
import com.montaury.pokebagarre.erreurs.ErreurRecuperationPokemon;
import com.montaury.pokebagarre.webapi.PokeBuildApi;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class BagarreTest {
    @Test
    void devrait_lever_une_exception_si_le_nom_du_second_pokemon_non_renseigne(){
        Bagarre bagarre = new Bagarre();
        Pokemon pokemon1 = new Pokemon("Pikachu", null, null);
        Pokemon pokemon2 = new Pokemon(null, null, null);

        Throwable thrown = catchThrowable(() -> bagarre.validerNomPokemons(pokemon1.getNom(), pokemon2.getNom()));

        assertThat(thrown).isInstanceOf(ErreurPokemonNonRenseigne.class).hasMessage("Le second pokemon n'est pas renseigne");

    }

    @Test
    void devrait_lever_une_exception_si_le_nom_du_premier_pokemon_non_renseigne(){
        Bagarre bagarre = new Bagarre();
        Pokemon pokemon1 = new Pokemon(null, null, null);
        Pokemon pokemon2 = new Pokemon("Dracaufeu", null, null);

        Throwable thrown = catchThrowable(() -> bagarre.validerNomPokemons(pokemon1.getNom(), pokemon2.getNom()));

        assertThat(thrown).isInstanceOf(ErreurPokemonNonRenseigne.class).hasMessage("Le premier pokemon n'est pas renseigne");

    }

    @Test
    void devrait_lever_une_exception_si_le_nom_du_second_pokemon_vide(){
        Bagarre bagarre = new Bagarre();
        Pokemon pokemon1 = new Pokemon("Pikachu", null, null);
        Pokemon pokemon2 = new Pokemon("", null, null);

        Throwable thrown = catchThrowable(() -> bagarre.validerNomPokemons(pokemon1.getNom(), pokemon2.getNom()));

        assertThat(thrown).isInstanceOf(ErreurPokemonNonRenseigne.class).hasMessage("Le second pokemon n'est pas renseigne");

    }

    @Test
    void devrait_lever_une_exception_si_le_nom_du_premier_pokemon_vide(){
        Bagarre bagarre = new Bagarre();
        Pokemon pokemon1 = new Pokemon("", null, null);
        Pokemon pokemon2 = new Pokemon("Dracaufeu", null, null);

        Throwable thrown = catchThrowable(() -> bagarre.validerNomPokemons(pokemon1.getNom(), pokemon2.getNom()));

        assertThat(thrown).isInstanceOf(ErreurPokemonNonRenseigne.class).hasMessage("Le premier pokemon n'est pas renseigne");

    }

    @Test
    void devrait_lever_une_exception_si_les_noms_des_pokemons_sont_identiques(){
        Bagarre bagarre = new Bagarre();
        Pokemon pokemon1 = new Pokemon("Dracaufeu", null, null);
        Pokemon pokemon2 = new Pokemon("Dracaufeu", null, null);

        Throwable thrown = catchThrowable(() -> bagarre.validerNomPokemons(pokemon1.getNom(), pokemon2.getNom()));

        assertThat(thrown).isInstanceOf(ErreurMemePokemon.class).hasMessage("Impossible de faire se bagarrer un pokemon avec lui-meme");
    }

    @Test
    void devrait_lever_une_exception_si_le_pokemon_un_pas_trouve_dans_api() {
        var fausseApi = Mockito.mock(PokeBuildApi.class);
        Bagarre bagarre = new Bagarre(fausseApi);
        String nom_pokemon_1 = "Pikachu";
        String nom_pokemon_2 = "Dracaufeu";

        Mockito.when(fausseApi.recupererParNom(nom_pokemon_1)).thenReturn(CompletableFuture.failedFuture(new ErreurRecuperationPokemon("Pikachu")));
        Mockito.when(fausseApi.recupererParNom(nom_pokemon_2)).thenReturn(CompletableFuture.completedFuture(new Pokemon("Dracaufeu", null, null)));

        CompletableFuture<Pokemon> futurResult = bagarre.demarrer(nom_pokemon_1, nom_pokemon_2);

        assertThat(futurResult)
                .failsWithin(Duration.ofSeconds(2))
                .withThrowableOfType(ExecutionException.class)
                .havingCause()
                .isInstanceOf(ErreurRecuperationPokemon.class)
                .withMessage("Impossible de recuperer les details sur 'Pikachu'") ;
    }

    @Test
    void devrait_lever_une_exception_si_le_pokemon_deux_pas_trouve_dans_api() {
        var fausseApi = Mockito.mock(PokeBuildApi.class);
        Bagarre bagarre = new Bagarre(fausseApi);
        String nom_pokemon_1 = "Pikachu";
        String nom_pokemon_2 = "Dracaufeu";

        Mockito.when(fausseApi.recupererParNom(nom_pokemon_1)).thenReturn(CompletableFuture.completedFuture(new Pokemon("Pikachu", null, null)));
        Mockito.when(fausseApi.recupererParNom(nom_pokemon_2)).thenReturn(CompletableFuture.failedFuture(new ErreurRecuperationPokemon("Dracaufeu")));

        CompletableFuture<Pokemon> futurResult = bagarre.demarrer(nom_pokemon_1, nom_pokemon_2);

        assertThat(futurResult)
                .failsWithin(Duration.ofSeconds(2))
                .withThrowableOfType(ExecutionException.class)
                .havingCause()
                .isInstanceOf(ErreurRecuperationPokemon.class)
                .withMessage("Impossible de recuperer les details sur 'Dracaufeu'") ;
    }

}