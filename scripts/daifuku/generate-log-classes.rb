require 'daifuku'
require 'erb'
require 'active_support/inflector'

ROOT_PATH = File.absolute_path(File.join(__dir__, '../../'))
LOG_DEFINITIONS = File.join(ROOT_PATH, 'scripts', 'daifuku', 'log_definitions')
TEMPLATE_DIR = File.join(ROOT_PATH, 'scripts', 'daifuku', 'templates')
DESTINATION_ROOT = File.join(ROOT_PATH, 'core', 'logs', 'src', 'commonMain', 'kotlin', 'me', 'matsumo', 'fanbox', 'core', 'logs')
KOTLIN_TYPE_MAPS = {
  'smallint' => 'Short',
  'integer' => 'Int',
  'bigint' => 'Long',
  'real' => 'Float',
  'double' => 'Double',
  'boolean' => 'Boolean',
  'string' => 'String',
  'date' => 'java.time.LocalDate',
  'timestamptz' => 'java.time.LocalDateTime',
}
UNSUPPORTED_TYPES = %w(timestamp)

class CategoryRepresentation
  def initialize(category)
    @category = category
  end

  def name
    @category.name
  end

  def class_name
    @category.name.camelize(:upper)
  end

  def descriptions
    @category.descriptions
  end

  def variable_name
    @category.name.camelize(:lower)
  end

  def events
    @events ||= @category.events.map { |_, event| EventRepresentation.new(event) }
  end

  def available_events
    events.reject(&:obsolete?)
  end
end

class EventRepresentation
  def initialize(event)
    @event = event
  end

  def name
    @event.name
  end

  def class_name
    @event.name.camelize(:upper)
  end

  def variable_name
    @event.name.camelize(:lower)
  end

  def columns
    @columns ||= @event.columns.reject(&:obsolete?).map { |column| ColumnRepresentation.new(column) }
  end

  def descriptions
    @event.descriptions
  end

  def pattern_matches
    columns.map { |column| "#{column.variable_name}" }&.join(', ')
  end

  def obsolete?
    @event.obsolete?
  end
end

class ColumnRepresentation
  def initialize(column)
    @column = column
  end

  def variable_name
    @column.name.camelize(:lower)
  end

  def original_name
    @column.name
  end

  def kotlin_type
    convert_to_kotlin_type(@column)
  end

  def descriptions
    @column.descriptions
  end

  def as_argument
    "#{variable_name}: #{kotlin_type}"
  end

  def custom_type?
    @column.type.custom?
  end

  private
  def convert_to_kotlin_type(column)
    type = column.type
    raise "Code builder currently doesn't support #{type.name}." if UNSUPPORTED_TYPES.include?(type.name)
    if type.custom?
      kotlin_type ||= type.name # custom type
    else
      kotlin_type = KOTLIN_TYPE_MAPS[type.name] # primitive types
    end
    if type.optional?
      "#{kotlin_type}?"
    else
      kotlin_type
    end
  end
end

class Generator
  def initialize
    @common_category = categories[COMMON_CATEGORY_NAME]
    raise 'Could not found common category. Please define common.md' unless @common_category
  end

  def generate_common_payload!(destination)
    columns = @common_category.common_columns.reject(&:obsolete?).map { |column| ColumnRepresentation.new(column) }
    template = File.open(File.join(TEMPLATE_DIR, 'CommonPayload.kt.erb')).read
    b = binding
    result = render(template, b)
    File.open(destination, 'w').write(result)
  end

  def generate_all_log_categories!(destination_dir)
    template = File.open(File.join(TEMPLATE_DIR, 'LogCategory.kt.erb')).read
    categories.each do |name, original_category|
      next if name == COMMON_CATEGORY_NAME
      destination = File.join(destination_dir, "#{original_category.name.camelize}Log.kt")
      category = CategoryRepresentation.new(original_category)
      b = binding
      result = render(template, b)
      File.open(destination, 'w').write(result)
    end
  end

  private
  def render(template, b)
    ERB.new(template, trim_mode: '-').result(b)
  end

  def categories
    @categories ||= Daifuku::Compiler.new.compile(LOG_DEFINITIONS)
  end
end

generator = Generator.new
generator.generate_common_payload!(File.join(DESTINATION_ROOT, 'CommonPayload.kt'))
generator.generate_all_log_categories!(File.join(DESTINATION_ROOT, 'category'))
