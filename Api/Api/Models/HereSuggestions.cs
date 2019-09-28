using System.Collections.Generic;

namespace Api.Models
{
    public class HereSuggestions
    {
        public IEnumerable<HereSuggestion> Suggestions { get; set; }
    }

    public class HereSuggestion
    {
        public string Label { get; set; }
        public string Language { get; set; }
        public string LocationId { get; set; }
        public HereAddress Address { get; set; }
        public string MatchLevel { get; set; }
    }

    public class HereAddress
    {
        public string Country { get; set; }
        public string State { get; set; }
        public string County { get; set; }
        public string City { get; set; }
        public string District { get; set; }
        public string Street { get; set; }
        public string HouseNumber { get; set; }
        public string PostalCode { get; set; }

        public string FullAddress
        {
            get {
                var city = string.IsNullOrEmpty(City) ? County : City;
                return $"{Country} {city} {District} {Street} {HouseNumber}".Trim();
            }
        }
    }
}
