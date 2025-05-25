package dev.datlag.mimasu.extension.movie;

/**
 * Information provided during movie requesting movie data.
 */
interface Request {

    /**
    * The movie id used for TMDB.
    * May be 0 which equals a null value.
    */
    int getTmdbId();

    /**
    * The movie id used for IMDB.
    * May be null or empty which equals a null value.
    */
    @nullable String getImdbId();

    /**
    * The movie id used for Wikidata.
    * May be null or empty which equals a null value.
    */
    @nullable String getWikidataId();

    /**
    * The movie title.
    * May be null or empty which equals a null value.
    */
    @nullable String getTitle();

    /**
    * The movie title in it's original language.
    * May be null or empty which equals a null value.
    */
    @nullable String getOriginalTitle();

    /**
    * The movie runtime in minutes.
    * May be 0 which equals a null value.
    */
    int getRuntimeInMinutes();
}