using System.Collections.Generic;

namespace Api.Models
{
    public class PlacesSuggestionResponse
    {
        public List<PlacesSuggestionResult> Results { get; set; }
    }

    public class PlacesSuggestionResult
    {
        public string Title { get; set; }
        public double[] Position { get; set; }

        public SuggesionAddress ToSuggestion()
        {
            if (Position == null)
                return null;

            return new SuggesionAddress
            {
                Title = Title,
                Coordinate = new Coordinate
                {
                    Latitude = Position[0],
                    Longitude = Position[1]
                }
            };
        }
    }
}
