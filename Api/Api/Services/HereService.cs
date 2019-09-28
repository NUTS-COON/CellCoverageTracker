using Api.Helpers;
using Api.Models;
using Api.Services.Interfaces;
using Api.Settings;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Api.Services
{
    public class HereService : IHereService
    {
        private readonly HereSettings _hereSettings;

        public HereService(HereSettings hereSettings)
        {
            _hereSettings = hereSettings;
        }

        private static NumberFormatInfo NFI = new NumberFormatInfo { NumberDecimalSeparator = "." };

        public async Task<HereRouteResponse> GetRoutes(DateTime time, Coordinate from, Coordinate to, string mode = "publicTransport")
        {
            var url = new StringBuilder()
                .Append("https://route.api.here.com/routing/7.2/calculateroute.json")
                .Append($"?app_id={_hereSettings.AppId}")
                .Append($"&app_code={_hereSettings.AppCode}")
                .Append($"&language=ru-ru")
                .Append($"&mode=fastest;{mode}")
                .Append($"&maneuverattributes=po,ti,pt,ac,di,fj,ix")
                .Append($"&routeattributes=sh,gr")
                .Append($"&waypoint0=geo!stopOver!{from.Latitude.ToString(NFI)},{from.Longitude.ToString(NFI)}")
                .Append($"&waypoint1=geo!stopOver!{to.Latitude.ToString(NFI)},{to.Longitude.ToString(NFI)}")
                .Append($"&departure={time.ToString("yyyy-MM-ddTHH:mm:ss")}")
                .ToString();

            return await HttpHepler.GetResult<HereRouteResponse>(url);
        }

        public async Task<Coordinate> GetCoordinate(string locationId)
        {
            if (string.IsNullOrEmpty(locationId))
                return null;

            var url = new StringBuilder()
                .Append("http://geocoder.api.here.com/6.2/geocode.json")
                .Append($"?locationid={locationId}")
                .Append($"&jsonattributes=1")
                .Append($"&gen=9")
                .Append($"&app_id={_hereSettings.AppId}")
                .Append($"&app_code={_hereSettings.AppCode}")
                .ToString();
            return (await HttpHepler.GetResult<HereGeocoder>(url))?.GetCoordinate();
        }

        public async Task<HereSuggestions> GetHereSuggestions(string text)
        {
            if (string.IsNullOrEmpty(text))
                return null;

            var url = new StringBuilder()
                .Append("http://autocomplete.geocoder.api.here.com/6.2/suggest.json")
                .Append($"?app_id={_hereSettings.AppId}")
                .Append($"&app_code={_hereSettings.AppCode}")
                .Append($"&query={text}")
                .ToString();

            return await HttpHepler.GetResult<HereSuggestions>(url);
        }

        public async Task<IEnumerable<SuggesionAddress>> GetSuggestions(string text)
        {
            var suggections = (await GetHereSuggestions(text))?.Suggestions;
            if (suggections == null || !suggections.Any())
                return Enumerable.Empty<SuggesionAddress>();

            return suggections.Select(suggection => new SuggesionAddress
            {
                LocationId = suggection.LocationId,
                Title = suggection.Address.FullAddress
            });
        }

        public async Task<PlacesSuggestionResponse> GetHerePlacesSuggestion(string text)
        {
            if (string.IsNullOrEmpty(text))
                return null;

            var url = new StringBuilder()
                .Append("https://places.demo.api.here.com/places/v1/autosuggest?at=55.6125538%2C55.6125538")
                .Append($"?q={text}")
                .Append("&at=55.6125538%2C55.6125538")
                .Append("&Accept-Language=ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
                .Append($"&app_id={_hereSettings.AppId}")
                .Append($"&app_code={_hereSettings.AppCode}")
                .ToString();

            return await HttpHepler.GetResult<PlacesSuggestionResponse>(url);
        }

        public async Task<IEnumerable<SuggesionAddress>> GetPlacesSuggestion(string text)
        {
            var result = await GetHerePlacesSuggestion(text);
            if (result?.Results == null || !result.Results.Any())
                return Enumerable.Empty<SuggesionAddress>();

            return result.Results.Select(p => p.ToSuggestion());
        }

        public async Task<IEnumerable<SuggesionAddress>> GetSuggestionsWithCoordinates(string text)
        {
            var suggections = (await GetHereSuggestions(text))?.Suggestions;
            if (suggections == null || !suggections.Any())
                return Enumerable.Empty<SuggesionAddress>();

            var result = new List<SuggesionAddress>();
            foreach(var suggection in suggections)
            {
                var coordinate = await GetCoordinate(suggection.LocationId);
                if (coordinate == null)
                    continue;

                result.Add(new SuggesionAddress
                {
                    Coordinate = coordinate,
                    Title = suggection.Address.FullAddress
                });
            }

            return result;
        }
    }
}
