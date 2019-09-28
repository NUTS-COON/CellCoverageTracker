using Api.Models;
using Api.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Api.Controllers
{
    [Route("api/[controller]/[action]")]
    [ApiController]
    public class SearchController : ControllerBase
    {
        private readonly IRouteSearcher _routeSearcher;
        private readonly IHereService _hereService;

        public SearchController(IRouteSearcher routeSearcher, IHereService hereService)
        {
            _routeSearcher = routeSearcher;
            _hereService = hereService;
        }

        [HttpPost]
        public async Task<List<TargetRoute>> FindRoutes([FromBody]RouteSearchInfo model)
        {
            return await _routeSearcher.GetHereRoutes(model.From, model.To, model.Time ?? DateTime.Now);
        }

        [HttpPost]
        public async Task<IEnumerable<SuggesionAddress>> GetSuggestions([FromBody]AddressText text)
        {
            return await _hereService.GetSuggestionsWithCoordinates(text?.Text);
        }
    }
}