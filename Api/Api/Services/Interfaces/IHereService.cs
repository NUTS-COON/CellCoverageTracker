﻿using Api.Models;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Api.Services.Interfaces
{
    public interface IHereService
    {
        Task<HereRouteResponse> GetRoutes(DateTime time, Coordinate from, Coordinate to, string mode = "publicTransport");
        Task<HereSuggestions> GetHereSuggestions(string text);
        Task<Coordinate> GetCoordinate(string locationId);
        Task<IEnumerable<SuggesionAddress>> GetSuggestionsWithCoordinates(string text);
        Task<IEnumerable<SuggesionAddress>> GetSuggestions(string text);
        Task<IEnumerable<SuggesionAddress>> GetPlacesSuggestion(string text);
    }
}
